package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.sidebar.SidebarManager;
import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.ITournamentEventListener;
import dev.necr0manthre.innotournament.util.ServerScheduler;
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
import net.minecraft.world.entity.Entity;
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

public class Tournament {
	private static final WeakHashMap<MinecraftServer, Tournament> serverToManagerMap = new WeakHashMap<>();

	public final Event<Consumer<Object>> onPrepare = EventFactory.createLoop();
	public final Event<Consumer<Object>> onStart = EventFactory.createLoop();
	public final Event<Consumer<Object>> onEnd = EventFactory.createLoop();
	public final Event<Consumer<Object>> onRemove = EventFactory.createLoop();
	public final Event<Consumer<TournamentPlayer>> playerRunOutOfLives = EventFactory.createLoop();
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
		serverToManagerMap.put(server, tournament);
	}

	public static Tournament get(MinecraftServer server) {
		return serverToManagerMap.getOrDefault(server, null);
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
		serverToManagerMap.remove(getServer());
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

	private void handlePreStartRespawn(ServerPlayer player, boolean huy, Entity.RemovalReason removalReason) {
		System.out.println("hello from " + this);

		player.setRespawnPosition(new ServerPlayer.RespawnConfig(tournamentPreSpawnDimension, tournamentPreSpawn, 0, true), false);
		player.teleport(new TeleportTransition(getServer().getLevel(tournamentPreSpawnDimension), tournamentPreSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
		}));
	}

	private void handleRespawn(ServerPlayer player, boolean huy, Entity.RemovalReason removalReason) {
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
			var tournamentPlayer = TournamentPlayerManager.get(getServer()).get(player);

			if (getTeamManager().getTeam(tournamentPlayer) == null) {
				tournamentPlayer.tournament = null;
				tournamentPlayer.player.setGameMode(GameType.SPECTATOR);
				var lastWarning = lastWarningTicks.computeIfAbsent(tournamentPlayer.player.getUUID(), u -> 0);
				if (getServer().getTickCount() - lastWarning > 1000) {
					lastWarningTicks.put(tournamentPlayer.player.getUUID(), getServer().getTickCount());
					tournamentPlayer.player.sendSystemMessage(
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
			if (tournamentPlayer.lives == 0) {
				tournamentPlayer.player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 3));
			}
			if (tournamentPlayer.tournament == null || tournamentPlayer.tournament.get() != this) {
				setupPlayer(tournamentPlayer);
			}
		}
	}

	public void checkAllPlayers() {
		for (var player : getServer().getPlayerList().getPlayers()) {
			checkPlayer(player);
		}
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
			var team = TournamentTeamManager.get(getServer()).get(player.getTeam());
			var teamName = team == null ? "No team" : team.getPlayerTeam().getName();
			var teamDisplayName = team == null ? null : team.getPlayerTeam().getDisplayName();
			List<ImmutableSidebarLine> lines = new ArrayList<>();
			int i = 0;
			lines.add(new ImmutableSidebarLine(i--, teamDisplayName == null ? Component.literal(teamName) : Component.empty().append(teamDisplayName).append(" (").append(Component.literal(teamName)).append(")"), BlankFormat.INSTANCE));
			if (team != null) {
				lines.add(new ImmutableSidebarLine(i--, Component.literal("Score: " + team.score), BlankFormat.INSTANCE));
				for (var tournamentPlayer : team.getPlayers()) {
					lines.add(new ImmutableSidebarLine(i--, Component.literal(tournamentPlayer.getName()).append("  [%d]".formatted(tournamentPlayer.lives)), BlankFormat.INSTANCE));
				}
			}
			lines.add(new ImmutableSidebarLine(i--, Component.literal(""), BlankFormat.INSTANCE));
			lines.add(new ImmutableSidebarLine(i--, Component.literal("Top teams: "), BlankFormat.INSTANCE));
			var top5 = getTopTeams(5);
			var top = getTopTeams(getTeamManager().getTeams().size());
			var index = top.indexOf(team);
			for (int j = 0; j < Math.min(5, top5.size()); j++) {
				if (top5.get(j) == team)
					lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 1) + "] ").append(team.getPlayerTeam().getDisplayName()).append(" (You) ").append("(" + team.score + ")"), BlankFormat.INSTANCE));
				else
					lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (j + 1) + "] ").append(top5.get(j).getPlayerTeam().getDisplayName()).append(" (" + top5.get(j).score + ")"), BlankFormat.INSTANCE));
			}
			if (top5.size() >= 5) {
				if (index >= 5) {
					if (index != 5)
						lines.add(new ImmutableSidebarLine(i--, Component.literal("..."), BlankFormat.INSTANCE));
					lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 1) + "] ").append(team.getPlayerTeam().getDisplayName()).append(" (You) ").append("(" + team.score + ")"), BlankFormat.INSTANCE));
				}
				if (index >= 4 && top.size() > index + 1) {
					lines.add(new ImmutableSidebarLine(i--, Component.literal("[" + (index + 2) + "] ").append(top.get(index + 1).getPlayerTeam().getDisplayName()).append(" (" + team.score + ")"), BlankFormat.INSTANCE));
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

		for (var team : TournamentTeamManager.get(getServer()).getTeams()) {
			setupTeam(team);
			team.acceptAll = false;
		}
		onStart.invoker().accept(null);
		checkAllPlayers();
		checkAllTeams();
	}

	public void checkAllTeams() {
		for (var team : getTeamManager().getTeams())
			checkTeam(team);
	}

	public void checkTeam(TournamentTeamManager.TournamentTeam team) {
		if (phase != 2)
			return;
		if (team.tournament == null || team.tournament.get() != this) {
			setupTeam(team);
		}
		if (team.owner == null || !team.getPlayers().contains(team.owner)) {
			if (team.getPlayers().isEmpty())
				getServer().getScoreboard().removePlayerTeam(team.getPlayerTeam());
			else
				team.owner = team.getPlayers().getFirst();
		}
	}

	private void prepareTick(MinecraftServer server) {
		checkAllPlayers();
	}

	private void tick(MinecraftServer server) {
		if (phase != 2)
			return;
		TournamentTeamManager.TournamentTeam t = null;
		boolean end = true;
		for (var team : getTeamManager().getTeams()) {
			boolean alive = false;
			for (var player : team.getPlayers()) {
				if (player.player != null && !player.player.hasDisconnected() && player.lives != 0) {
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
		if (getTeamManager().getTeams().isEmpty())
			end = false;
		if (end) {
			if (t != null) {
				t.score += lastTeamScore;
				updateSidebars();
			}
			var top = getTopTeams(getTeamManager().getTeams().size());
			getServer().getPlayerList().broadcastSystemMessage(Component.literal("Турнир завершён! Победила команда ").append(top.get(0).getPlayerTeam().getDisplayName()).append(" (" + top.get(0).getPlayers().stream().map(TournamentPlayer::getName).collect(Collectors.joining(" ")) + " )"), false);
			for (int i = 0; i < top.size(); i++) {
				getServer().getPlayerList().broadcastSystemMessage(Component.literal("[" + (i + 1) + "] ").append(top.get(i).getPlayerTeam().getDisplayName()).append(" " + top.get(i).score).append(" (" + top.get(i).getPlayers().stream().map(TournamentPlayer::getName).collect(Collectors.joining(" ")) + " )"), false);
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
//				ServerScheduler.scheduleServerPost(0, () -> {
//					try {
//						event.remove();
//					} catch (Exception ignored) {
//					}
//				});
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

	public TournamentPlayerManager getPlayerManager() {
		return TournamentPlayerManager.get(getServer());
	}

	public TournamentTeamManager getTeamManager() {
		return TournamentTeamManager.get(getServer());
	}

	private EventResult livingDeath(LivingEntity entity, DamageSource source) {
		if (entity instanceof ServerPlayer player) {
			var tournamentPlayer = getPlayerManager().get(player);
			if (tournamentPlayer.lives > 0) {
				tournamentPlayer.lives--;
				updateSidebars();
				if (tournamentPlayer.lives == 0) playerRunOutOfLives.invoker().accept(tournamentPlayer);
			}
		}
		return EventResult.pass();
	}

	public void setupPlayer(TournamentPlayer player) {
		player.tournament = new WeakReference<>(this);
		player.lives = 3;
		player.player.heal(100);
		for (var adv : getServer().getAdvancements().getAllAdvancements())
			for (var huy : player.player.getAdvancements().getOrStartProgress(adv).getCompletedCriteria())
				player.player.getAdvancements().revoke(adv, huy);
		player.player.getInventory().clearContent();
		player.player.getFoodData().setFoodLevel(100);
		player.player.getFoodData().setSaturation(1);
		player.player.teleport(new TeleportTransition(getServer().getLevel(tournamentSpawnDimension), tournamentSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
		}));
		player.player.setGameMode(GameType.SURVIVAL);
		player.tournament = new WeakReference<>(this);
	}

	public void setupTeam(TournamentTeamManager.TournamentTeam team) {
		team.score = 0;
		team.clearAdvancements();
		team.tournament = new WeakReference<>(this);
	}

	public void addScores(TournamentPlayer player, double score) {
		if (player.lives == 0) return;
		getTeamManager().getTeam(player).score += score * teamScoreModifiers[getTeamManager().getTeam(player).getPlayers().size()];
		updateSidebars();
	}

	public void addScores(TournamentTeamManager.TournamentTeam team, double score) {
		team.score += score * teamScoreModifiers[team.getPlayers().size()];
		updateSidebars();
	}

	public List<TournamentTeamManager.TournamentTeam> getTopTeams(int n) {
		var sorted = getTeamManager().getTeams().stream().sorted(Comparator.comparing(t -> -t.score)).toList();
		if (sorted.size() < n)
			return sorted;
		return sorted.subList(0, n);
	}

	public void addPlayerToTeam(TournamentPlayer player, TournamentTeamManager.TournamentTeam team) {
		if (getTeamManager().getTeam(player) != null)
			getTeamManager().getTeam(player).removePlayer(player);
		var coeff = teamScoreModifiers[team.getPlayers().size()];
		team.addPlayer(player);
		team.score *= teamScoreModifiers[team.getPlayers().size()] / coeff;
	}
}
