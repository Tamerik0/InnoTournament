package dev.necr0manthre.innotournament.teams.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.players.PlayerManager;
import dev.necr0manthre.innotournament.teams.PlayerJoinResult;
import dev.necr0manthre.innotournament.teams.PlayerKickResult;
import dev.necr0manthre.innotournament.teams.events.PlayerTeamEvent;
import dev.necr0manthre.innotournament.util.ServerBoundObjManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
public class TeamManager implements ServerBoundObjManager.Removable {
    private static final ServerBoundObjManager<TeamManager> managerMap = new ServerBoundObjManager<>();
    private static final Map<Entity, TeamManager> entityManagerMap = new WeakHashMap<>();
    private final WeakReference<MinecraftServer> serverRef;
    private final BiMap<PlayerTeam, Entity> teamEntityMap = HashBiMap.create();
    private final Dominion ecs = Dominion.create();
    public static final Event<Consumer<PlayerTeamEvent>> PLAYER_LEAVE_TEAM_EVENT = EventFactory.createLoop();
    public static final Event<Consumer<PlayerTeamEvent>> PLAYER_JOIN_TEAM_EVENT = EventFactory.createLoop();

    public TeamManager(MinecraftServer server) {
        this.serverRef = new WeakReference<>(server);
    }

    public static TeamManager get(MinecraftServer server) {
        return managerMap.computeIfAbsent(server, TeamManager::new);
    }

    public static TeamManager get(Entity team) {
        return Objects.requireNonNull(entityManagerMap.get(team));
    }

    public MinecraftServer getServer() {
        return Objects.requireNonNull(serverRef.get());
    }

    public Entity getEntity(PlayerTeam team) {
        return teamEntityMap.computeIfAbsent(team, o -> {
            var entity = ecs.createEntity();
            entityManagerMap.put(entity, this);
            return entity;
        });
    }

    public static void register() {
        managerMap.register();
    }

    public static void unregister() {
        managerMap.unregister();
    }

    public void remove() {
        ecs.close();
    }

    @Override
    public void onRemove() {
        remove();
    }

    public static PlayerTeam getPlayerTeam(Entity team) {
        var playerTeam = get(team).teamEntityMap.inverse().get(team);
        if (playerTeam == null)
            throw new IllegalArgumentException("Entity is not mapped to a team");
        return playerTeam;
    }

    public static Optional<Entity> getPlayersTeam(Entity player) {
        var playerName = PlayerManager.getName(player).orElseThrow();
        return Optional.ofNullable(PlayerManager.get(player).getServer().getScoreboard().getPlayersTeam(playerName)).map(get(PlayerManager.get(player).getServer())::getEntity);
    }

    public List<Entity> getAllTeams() {
        return getServer().getScoreboard().getPlayerTeams().stream().map(this::getEntity).toList();
    }

    public static void forceRemovePlayer(Entity team, Entity player) {
        get(team).getServer().getScoreboard().removePlayerFromTeam(PlayerManager.getName(player).orElseThrow(), getPlayerTeam(team));
    }

    public static void forceAddPlayer(Entity team, Entity player) {
        get(team).getServer().getScoreboard().addPlayerToTeam(PlayerManager.getName(player).orElseThrow(), getPlayerTeam(team));
    }

    public static List<Entity> getPlayers(Entity team) {
        var playerTeam = getPlayerTeam(team);
        return playerTeam.getPlayers().stream().map(name -> PlayerManager.get(get(team).getServer()).getEntity(name).orElseThrow(() -> new RuntimeException("Cannot get ECS entity for player with name " + name))).toList();
    }

    public static boolean changeName(Entity team, String newName) {
        var lastTeam = get(team).teamEntityMap.inverse().get(team);
        var scoreboard = get(team).getServer().getScoreboard();
        if (scoreboard.getPlayerTeam(newName) != null)
            return false;
        var playerTeam = scoreboard.addPlayerTeam(newName);
        for (var member : lastTeam.getPlayers()) {
            scoreboard.addPlayerToTeam(member, playerTeam);
        }
        playerTeam.setAllowFriendlyFire(lastTeam.isAllowFriendlyFire());
        playerTeam.setColor(lastTeam.getColor());
        playerTeam.setPlayerPrefix(lastTeam.getPlayerPrefix());
        playerTeam.setPlayerSuffix(lastTeam.getPlayerSuffix());
        playerTeam.setDisplayName(lastTeam.getDisplayName());
        playerTeam.setCollisionRule(lastTeam.getCollisionRule());
        playerTeam.setDeathMessageVisibility(lastTeam.getDeathMessageVisibility());
        playerTeam.setNameTagVisibility(lastTeam.getNameTagVisibility());
        playerTeam.setSeeFriendlyInvisibles(lastTeam.canSeeFriendlyInvisibles());
        scoreboard.removePlayerTeam(lastTeam);
        get(team).teamEntityMap.inverse().put(team, playerTeam);
        return true;
    }

    public static PlayerJoinResult tryAddPlayerToTeam(Entity team, Entity player) {
        var playersTeam = getPlayersTeam(player);
        if (playersTeam.isPresent()) {
            if (playersTeam.get() == team)
                return PlayerJoinResult.ALREADY_IN_TEAM;
            var kickResult = tryKickPlayerFromTeam(player);
            if (!kickResult.isSuccess())
                return PlayerJoinResult.cannotKick(kickResult);
        }
        var event = new PlayerTeamEvent(player, team);
        PLAYER_JOIN_TEAM_EVENT.invoker().accept(event);
        if (event.isCanceled())
            return PlayerJoinResult.CANCELLED;
        forceAddPlayer(team, player);
        return PlayerJoinResult.SUCCESS;
    }

    public static PlayerKickResult tryKickPlayerFromTeam(Entity team, Entity player) {
        var playerName = PlayerManager.getName(player).orElseThrow();
        var playerTeam = getPlayerTeam(team);
        if (!playerTeam.getPlayers().contains(playerName))
            return PlayerKickResult.NOT_IN_TEAM;
        var event = new PlayerTeamEvent(player, team);
        PLAYER_LEAVE_TEAM_EVENT.invoker().accept(event);
        if (event.isCanceled())
            return PlayerKickResult.CANCELLED;
        forceRemovePlayer(team, player);
        return PlayerKickResult.SUCCESS;
    }

    public static PlayerKickResult tryKickPlayerFromTeam(Entity player) {
        var team = getPlayersTeam(player);
        return team.map(entity -> tryKickPlayerFromTeam(entity, player)).orElse(PlayerKickResult.NOT_IN_TEAM);
    }
}
