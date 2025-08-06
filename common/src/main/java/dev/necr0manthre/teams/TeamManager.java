package dev.necr0manthre.teams;

import dev.dominion.ecs.api.Dominion;
import dev.necr0manthre.innotournament.util.ServerBoundObjManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

public class TeamManager implements ServerBoundObjManager.Removable {
    private static final ServerBoundObjManager<TeamManager> managerMap = new ServerBoundObjManager<>();
    private final WeakReference<MinecraftServer> serverRef;
    private final WeakHashMap<PlayerTeam, Team> teams = new WeakHashMap<>();
    public final Dominion ecs = Dominion.create();

    private TeamManager(MinecraftServer server) {
        this.serverRef = new WeakReference<>(server);
    }

    public static TeamManager get(MinecraftServer server) {
        return managerMap.computeIfAbsent(server, TeamManager::new);
    }

    public MinecraftServer getServer() {
        return serverRef.get();
    }

    public Team get(PlayerTeam playerTeam) {
        if (playerTeam == null)
            return null;
        return teams.computeIfAbsent(playerTeam, t-> {
            var team = new Team(this);
            attachTeamEntity(team, playerTeam);
            return team;
        });
    }

    public Team createTeamEntity(boolean persistent) {
        var team = new Team(this);
        team.setPersistent(persistent);
        return team;
    }

    public void attachTeamEntity(Team team, PlayerTeam playerTeam) {
        team.playerTeam = playerTeam;
        teams.put(playerTeam, team);
    }

    public static void register() {
        managerMap.register();
        TeamsEventHandler.init();
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

    public List<Team> getAllTeams(){
        return getServer().getScoreboard().getPlayerTeams().stream().map(this::get).toList();
    }
}
