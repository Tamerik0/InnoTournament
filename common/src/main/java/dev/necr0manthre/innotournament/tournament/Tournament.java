package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.players.PlayerManager;
import dev.necr0manthre.innotournament.sidebar.SidebarManager;
import dev.necr0manthre.innotournament.teams.components.TeamOwner;
import dev.necr0manthre.innotournament.teams.components.TeamSettings;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import dev.necr0manthre.innotournament.tournament.components.TeamAdvancements;
import dev.necr0manthre.innotournament.tournament.components.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.components.TeamScore;
import dev.necr0manthre.innotournament.tournament.components.TournamentTeam;
import dev.necr0manthre.innotournament.tournament.events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament.events.ITournamentEventListener;
import dev.necr0manthre.innotournament.util.ServerBoundObjManager;
import dev.necr0manthre.innotournament.util.ServerScheduler;
import dev.necr0manthre.innotournament.util.TournamentUtils;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.SidebarInterface;
import eu.pb4.sidebars.api.lines.ImmutableSidebarLine;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tournament implements ServerBoundObjManager.Removable {
    private static final ServerBoundObjManager<Tournament> tournamentManager = new ServerBoundObjManager<>();
    public final Event<Consumer<Object>> onPrepare = EventFactory.createLoop();
    public final Event<Consumer<Object>> onStart = EventFactory.createLoop();
    public final Event<Consumer<Object>> onEnd = EventFactory.createLoop();
    public final Event<Consumer<Object>> onRemove = EventFactory.createLoop();
    public final Event<Consumer<Entity>> playerRunOutOfLives = EventFactory.createLoop();
    private final GameType preStartGameMode;
    private final Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> preStartEvents;
    private final Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> mainEvents;
    public final BlockPos tournamentSpawn;
    public final ResourceKey<Level> tournamentSpawnDimension;
    public final TournamentAdvancementHandler advancementHandler = new TournamentAdvancementHandler(this);
    BlockPos tournamentPreSpawn;
    ResourceKey<Level> tournamentPreSpawnDimension;
    ResourceLocation startBoxStructureResourceLocation;
    BlockPos startBoxPos;
    long startTime;
    public final float lastTeamScore;
    public double[] teamScoreModifiers;
    @Getter
    int phase = 0;
    private StructureTemplate startBoxStructure;
    private WeakReference<MinecraftServer> serverRef = new WeakReference<>(null);
    private SidebarManager sidebarManager;

    public Tournament(BlockPos tournamentSpawn, ResourceKey<Level> tournamentSpawnDimension, BlockPos tournamentPreSpawn, ResourceKey<Level> tournamentPreSpawnDimension, ResourceLocation startBoxStructureResourceLocation, BlockPos startBoxPos, GameType preStartGameMode, Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> preStartEvents, Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> mainEvents, double[] teamScoreModifiers, float lastTeamScore) {
        this.tournamentSpawn = tournamentSpawn;
        this.tournamentSpawnDimension = tournamentSpawnDimension;
        this.tournamentPreSpawn = tournamentPreSpawn;
        this.tournamentPreSpawnDimension = tournamentPreSpawnDimension;
        this.startBoxStructureResourceLocation = startBoxStructureResourceLocation;
        this.startBoxPos = startBoxPos;
        this.preStartGameMode = preStartGameMode;
        this.preStartEvents = preStartEvents;
        this.mainEvents = mainEvents;
        this.teamScoreModifiers = teamScoreModifiers;
        this.lastTeamScore = lastTeamScore;
    }

    public static void setTournament(MinecraftServer server, Tournament tournament) {
        if (get(server) != null) try {
            get(server).remove();
        } catch (Exception e) {
            throw new RuntimeException("Exception while tournament", e);
        }
        tournamentManager.put(server, tournament);
    }

    public static Tournament get(MinecraftServer server) {
        return tournamentManager.getOrDefault(server, null);
    }

    public MinecraftServer getServer() {
        return serverRef.get();
    }

    public void updateSidebars() {
        for (var player : getServer().getPlayerList().getPlayers())
            sidebarManager.updateSidebar(player);
    }

    public void remove() {
        System.out.println(this + " is being removed");
        onRemove.invoker().accept(null);
        for (var event : preStartEvents.keySet()) {
            event.remove();
        }
        for (var event : mainEvents.keySet()) {
            event.remove();
        }
        sidebarManager.remove();
        PlayerEvent.PLAYER_RESPAWN.unregister(handlePreStartRespawn);
        PlayerEvent.PLAYER_RESPAWN.unregister(handleRespawn);
        TickEvent.SERVER_POST.unregister(tick);
        TickEvent.SERVER_POST.unregister(prepareTick);
        EntityEvent.LIVING_DEATH.unregister(livingDeath);
        advancementHandler.unregister();
        System.out.println(this + " is removed");
    }

    public void placeStartBox() {
        startBoxStructure = getServer().getStructureManager().getOrCreate(startBoxStructureResourceLocation);
        startBoxStructure.placeInWorld(getServer().getLevel(tournamentPreSpawnDimension), startBoxPos, startBoxPos, new StructurePlaceSettings(), new LegacyRandomSource(0), 2 | 816);
    }

    public void removeStartBox() {
        var level = getServer().getLevel(tournamentPreSpawnDimension);
        for (var pos : BlockPos.betweenClosed(startBoxPos, startBoxPos.offset(startBoxStructure.getSize()))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
        }
    }

    private void handlePreStartRespawn(ServerPlayer player, boolean huy, net.minecraft.world.entity.Entity.RemovalReason removalReason) {
        System.out.println("hello from " + this);

        player.setRespawnPosition(new ServerPlayer.RespawnConfig(tournamentPreSpawnDimension, tournamentPreSpawn, 0, true), false);
        player.teleport(new TeleportTransition(getServer().getLevel(tournamentPreSpawnDimension), tournamentPreSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
        }));
    }

    private void handleRespawn(ServerPlayer player, boolean huy, net.minecraft.world.entity.Entity.RemovalReason removalReason) {
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(tournamentSpawnDimension, tournamentSpawn, 0, true), false);
        player.teleport(new TeleportTransition(getServer().getLevel(tournamentSpawnDimension), tournamentSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
        }));
    }

    private HashMap<UUID, Integer> lastWarningTicks = new HashMap<>();

    public void checkPlayer(ServerPlayer player) {
        if (phase == 1) {
            if (!player.hasPermissions(4)) {
                if (player.gameMode.getGameModeForPlayer() != preStartGameMode) player.setGameMode(preStartGameMode);
                if (startBoxStructure != null) {
                    if (!AABB.of(startBoxStructure.getBoundingBox(new StructurePlaceSettings(), startBoxPos)).contains(player.position())) {
                        player.kill(player.serverLevel());
                    }
                }
            }

        } else if (phase == 2) {
            var ecsPlayer = PlayerManager.getEntity(player);
            var playerData = TournamentPlayer.getTournamentData(ecsPlayer);
            if (TeamManager.getPlayersTeam(ecsPlayer).isEmpty()) {
                playerData.tournament = null;
                player.setGameMode(GameType.SPECTATOR);
                var lastWarning = lastWarningTicks.computeIfAbsent(player.getUUID(), u -> 0);
                if (getServer().getTickCount() - lastWarning > 1000) {
                    lastWarningTicks.put(player.getUUID(), getServer().getTickCount());
                    player.sendSystemMessage(
                            Component.literal("You don't have team. But you must have team to participate. You can create team with only you through ")
                                    .append(Component.literal("/teams create <YourAwesomeTeamName>")
                                            .withStyle(ChatFormatting.AQUA)
                                            .withStyle(style -> style
                                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("<Yea, it's clickable>")))
                                                    .withClickEvent(new ClickEvent.SuggestCommand("/teams create "))))
                                    .append(Component.literal(" . But playing alone sucks, for more info see"))
                                    .append(Component.literal("/tournament help")
                                            .withStyle(ChatFormatting.AQUA)
                                            .withStyle(style -> style
                                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("<Yea, it's clickable>")))
                                                    .withClickEvent(new ClickEvent.RunCommand("/tournament help"))))
                                    .append(" .(It's really important)"));
                }
                return;
            }
            if (playerData.lives == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 3));
            }
            if (playerData.tournament == null || playerData.tournament.get() != this) {
                setupPlayer(player);
            }
        }
    }

    public void checkAllPlayers() {
        for (var player : new ArrayList<>(getServer().getPlayerList().getPlayers())) {
            checkPlayer(player);
        }
    }

    public TeamManager teamManager() {
        return TeamManager.get(getServer());
    }

    TickEvent.Server prepareTick = this::prepareTick;
    PlayerEvent.PlayerRespawn handlePreStartRespawn = this::handlePreStartRespawn;
    EntityEvent.LivingDeath livingDeath = this::livingDeath;
    PlayerEvent.PlayerRespawn handleRespawn = this::handleRespawn;
    TickEvent.Server tick = this::tick;

    public void prepare(MinecraftServer server) {
        if (phase == 1) throw new IllegalStateException("Tournament is already prepared");
        if (phase == 2) throw new IllegalStateException("Tournament is already started");
        if (phase == 3) throw new IllegalStateException("Tournament is already ended");
        if (phase > 3) throw new IllegalStateException("WTF with tournament phase");
        serverRef = new WeakReference<>(server);
        TickEvent.SERVER_POST.register(prepareTick);
        this.sidebarManager = new SidebarManager(server, player -> new SidebarManager.SuppliedSidebarData(Sidebar.Priority.OVERRIDE, 400, () -> {
            var title = Component.literal("Innotournament v2!!!");
            var playerTeam = player.getTeam();
            var teamDisplayName = playerTeam == null ? null : playerTeam.getDisplayName();
            List<ImmutableSidebarLine> lines = new ArrayList<>();
            int i = 0;
            var team = playerTeam == null ? null : teamManager().getEntity(playerTeam);
            lines.add(new ImmutableSidebarLine(i--, team == null ? Component.literal("You have no team") : Component.literal("Your team: ").append(teamDisplayName), BlankFormat.INSTANCE));
            if (team != null) {
                lines.add(new ImmutableSidebarLine(i--, Component.literal("Score: " + TournamentUtils.formatTeamScore(team)), BlankFormat.INSTANCE));
                for (var tournamentPlayer : TeamManager.getPlayers(team)) {
                    lines.add(new ImmutableSidebarLine(i--, Component.empty().append(PlayerManager.getDisplayName(tournamentPlayer).orElse(Component.literal("Unknown"))).append("  [%d]".formatted(TournamentPlayer.getTournamentData(tournamentPlayer).lives)), BlankFormat.INSTANCE));
                }
            }

            lines.add(new ImmutableSidebarLine(i--, Component.literal(""), BlankFormat.INSTANCE));
            lines.add(new ImmutableSidebarLine(i--, Component.literal("Top teams: "), BlankFormat.INSTANCE));
            var top5 = getTopTeams(5);
            var top = getTopTeams(teamManager().getAllTeams().size());
            var index = top.indexOf(team);
            for (int j = 0; j < Math.min(5, top5.size()); j++) {
                if (top5.get(j) == team)
                    lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 1) + "] ").append(playerTeam.getDisplayName()).append(" (You) ").append("(" + TournamentUtils.formatTeamScore(team) + ")"), BlankFormat.INSTANCE));
                else
                    lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (j + 1) + "] ").append(TeamManager.getPlayerTeam(top5.get(j)).getDisplayName()).append(" (" + TournamentUtils.formatTeamScore(top5.get(j)) + ")"), BlankFormat.INSTANCE));
            }
            if (top5.size() >= 5) {
                if (index >= 5) {
                    if (index != 5)
                        lines.add(new ImmutableSidebarLine(i--, Component.literal("..."), BlankFormat.INSTANCE));
                    lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 1) + "] ").append(TeamManager.getPlayerTeam(team).getDisplayName()).append(" (You) ").append("(" + TournamentUtils.formatTeamScore(team) + ")"), BlankFormat.INSTANCE));
                }
                if (index >= 4 && top.size() > index + 1) {
                    lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 2) + "] ").append(TeamManager.getPlayerTeam(top.get(index + 1)).getDisplayName()).append(" (" + TournamentUtils.formatTeamScore(team) + ")"), BlankFormat.INSTANCE));
                }
            }
            return new SidebarInterface.SidebarData(title, lines);
        }));
        server.getLevel(tournamentPreSpawnDimension).setDefaultSpawnPos(tournamentPreSpawn, 0);
        server.getLevel(tournamentPreSpawnDimension).getWorldBorder().setCenter(tournamentPreSpawn.getX(), tournamentPreSpawn.getZ());
        PlayerEvent.PLAYER_RESPAWN.register(handlePreStartRespawn);
//		PlayerEvent.PLAYER_JOIN.register(checkPlayer);
        EntityEvent.LIVING_DEATH.register(livingDeath);
        phase = 1;
        for (var event : preStartEvents.keySet()) {
            event.subscribe(this);
            for (var listener : preStartEvents.get(event))
                event.addListener((ITournamentEventListener) listener);
        }
        checkAllPlayers();
        onPrepare.invoker().accept(null);
        advancementHandler.register();
    }

    public void start() {
        if (phase == 0) throw new IllegalStateException("Tournament is not prepared yet");
        if (phase == 2) throw new IllegalStateException("Tournament is already started");
        if (phase == 3) throw new IllegalStateException("Tournament is already ended");
        if (phase > 3) throw new IllegalStateException("WTF with tournament phase");
        for (var event : mainEvents.keySet()) {
            event.subscribe(this);
            for (var listener : mainEvents.get(event))
                event.addListener((ITournamentEventListener) listener);
        }
        for (var event : preStartEvents.keySet()) {
            event.remove();
        }
        phase = 2;
        PlayerEvent.PLAYER_RESPAWN.unregister(handlePreStartRespawn);
        PlayerEvent.PLAYER_RESPAWN.register(handleRespawn);
        TickEvent.SERVER_POST.register(tick);
        startTime = getServer().overworld().getGameTime();
        if (tournamentSpawnDimension.equals(Level.OVERWORLD)) {
            getServer().overworld().getWorldBorder().setCenter(tournamentSpawn.getX(), tournamentSpawn.getZ());
            getServer().getLevel(Level.NETHER).getWorldBorder().setCenter(tournamentSpawn.getX() * 8, tournamentSpawn.getZ() * 8);
            getServer().getLevel(Level.END).getWorldBorder().setCenter(0, 0);
        } else if (tournamentSpawnDimension.equals(Level.NETHER)) {
            getServer().getLevel(Level.NETHER).getWorldBorder().setCenter(tournamentSpawn.getX(), tournamentSpawn.getZ());
            getServer().overworld().getWorldBorder().setCenter(tournamentSpawn.getX() * 8, tournamentSpawn.getZ() * 8);
            getServer().getLevel(Level.END).getWorldBorder().setCenter(0, 0);
        } else {
            getServer().getLevel(Level.END).getWorldBorder().setCenter(tournamentSpawn.getX(), tournamentSpawn.getZ());
            getServer().overworld().getWorldBorder().setCenter(tournamentSpawn.getX(), tournamentSpawn.getZ());
            getServer().getLevel(Level.END).getWorldBorder().setCenter(tournamentSpawn.getX() / 8, tournamentSpawn.getZ() / 8);
        }
        getServer().getLevel(tournamentSpawnDimension).setDefaultSpawnPos(tournamentSpawn, 0);

        for (var team : teamManager().getAllTeams()) {
            setupTeam(team);
            TeamSettings.getSettings(team).acceptAll = false;
        }
        onStart.invoker().accept(null);
        checkAllPlayers();
        checkAllTeams();
    }

    public void checkAllTeams() {
        for (var team : teamManager().getAllTeams())
            checkTeam(team);
    }

    public void checkTeam(Entity team) {
        if (phase != 2)
            return;
        var tournamentTeam = TournamentTeam.get(team);
        if (tournamentTeam.tournament == null || tournamentTeam.tournament.get() != this) {
            setupTeam(team);
        }
        if (TeamOwner.getTeamOwner(team) == null || !TeamManager.getPlayers(team).contains(TeamOwner.getTeamOwner(team))) {
            if (TeamManager.getPlayers(team).isEmpty())
                getServer().getScoreboard().removePlayerTeam(TeamManager.getPlayerTeam(team));
            else
                TeamOwner.setTeamOwner(team, TeamManager.getPlayers(team).getFirst());
        }
    }

    private void prepareTick(MinecraftServer server) {
        checkAllPlayers();
    }

    private void tick(MinecraftServer server) {
        if (phase != 2)
            return;
        Entity t = null;
        boolean end = true;
        for (var team : teamManager().getAllTeams()) {
            boolean alive = false;
            for (var player : TeamManager.getPlayers(team)) {
                var serverPlayer = PlayerManager.getServerPlayer(player);
                if (serverPlayer.isPresent() && !serverPlayer.get().hasDisconnected() && TournamentPlayer.getTournamentData(player).lives != 0) {
                    alive = true;
                    break;
                }
            }
            if (alive) {
                if (t == null)
                    t = team;
                else
                    end = false;
            }
        }
        if (teamManager().getAllTeams().isEmpty())
            end = false;
        if (end) {
            if (t != null) {
                TeamScore.getScoreComponent(t).score += lastTeamScore;
                updateSidebars();
            }
            var top = getTopTeams(teamManager().getAllTeams().size());
            getServer().getPlayerList().broadcastSystemMessage(Component.literal("Турнир завершён! Победила команда ").append(TeamManager.getPlayerTeam(top.get(0)).getDisplayName()).append(" (" + TeamManager.getPlayers(top.get(0)).stream().map(PlayerManager::getName).map(opt -> opt.orElse("Unknown")).collect(Collectors.joining(" ")) + " )"), false);
            for (int i = 0; i < top.size(); i++) {
                getServer().getPlayerList().broadcastSystemMessage(Component.literal("[" + (i + 1) + "] ").append(TeamManager.getPlayerTeam(top.get(i)).getDisplayName()).append(" " + TournamentUtils.formatTeamScore(top.get(i))).append(" (" + TeamManager.getPlayers(top.get(i)).stream().map(PlayerManager::getName).map(opt -> opt.orElse("Unknown")).collect(Collectors.joining(" ")) + " )"), false);
            }
            onEnd.invoker().accept(null);
            phase = 3;

            for (var event : mainEvents.keySet()) {
                ServerScheduler.scheduleServerPre(0, () -> {
                    try {
                        event.remove();
                    } catch (Exception ignored) {
                    }
                });
            }

            ServerScheduler.scheduleServerPre(0, () -> {
                PlayerEvent.PLAYER_RESPAWN.unregister(handleRespawn);
                TickEvent.SERVER_POST.unregister(tick);
                EntityEvent.LIVING_DEATH.unregister(livingDeath);
                advancementHandler.unregister();
            });
            return;
        }
        checkAllPlayers();
        checkAllTeams();
    }


    public long getElapsedTime() {
        if (phase >= 2) return getServer().overworld().getGameTime() - startTime;
        return 0;
    }

    private EventResult livingDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer player) {
            var tournamentPlayer = PlayerManager.getEntity(player);
            var data = TournamentPlayer.getTournamentData(tournamentPlayer);
            if (data.lives > 0) {
                data.lives--;
                updateSidebars();
                if (data.lives == 0) playerRunOutOfLives.invoker().accept(tournamentPlayer);
            }
        }
        return EventResult.pass();
    }

    public void setupPlayer(ServerPlayer player) {
        var playerData = TournamentPlayer.getTournamentData(PlayerManager.getEntity(player));
        playerData.tournament = new WeakReference<>(this);
        playerData.lives = 3;
        player.heal(100);
        for (var adv : getServer().getAdvancements().getAllAdvancements())
            for (var huy : player.getAdvancements().getOrStartProgress(adv).getCompletedCriteria())
                player.getAdvancements().revoke(adv, huy);
        player.getInventory().clearContent();
        player.getFoodData().setFoodLevel(100);
        player.getFoodData().setSaturation(1);
        player.teleport(new TeleportTransition(getServer().getLevel(tournamentSpawnDimension), tournamentSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
        }));
        player.setGameMode(GameType.SURVIVAL);
        playerData.tournament = new WeakReference<>(this);
    }

    public void setupTeam(Entity team) {
        TeamScore.setTeamScore(team, 0);
        TeamAdvancements.get(team).advancements.clear();
        TournamentTeam.get(team).tournament = new WeakReference<>(this);
    }

    public void addScoresToPlayer(Entity player, double score) {
        if (TournamentPlayer.getTournamentData(player).lives == 0) return;
        var team = TeamManager.getPlayersTeam(player).orElseThrow();
        TeamScore.getScoreComponent(team).score += score * teamScoreModifiers[TeamManager.getPlayers(team).size()];
        updateSidebars();
    }

    public void addScores(Entity team, double score) {
        TeamScore.getScoreComponent(team).score += score * teamScoreModifiers[TeamManager.getPlayers(team).size()];
        updateSidebars();
    }

    public List<Entity> getTopTeams(int n) {
        var sorted = teamManager().getAllTeams().stream().sorted(Comparator.comparing((Entity t) -> -TeamScore.getTeamScore(t))).toList();
        if (sorted.size() < n)
            return sorted;
        return sorted.subList(0, n);
    }

    @Override
    public void onRemove() {
        remove();
    }
}
