package dev.necr0manthre.innotournament.tournament;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

public class TournamentTeamManager {
	private static final WeakHashMap<MinecraftServer, TournamentTeamManager> serverToManagerMap = new WeakHashMap<>();
	private final WeakReference<MinecraftServer> serverRef;
	WeakHashMap<PlayerTeam, TournamentTeam> teams = new WeakHashMap<>();

	private TournamentTeamManager(MinecraftServer server) {
		this.serverRef = new WeakReference<>(server);
	}

	public static TournamentTeamManager get(MinecraftServer server) {
		return serverToManagerMap.computeIfAbsent(server, TournamentTeamManager::new);
	}

	public MinecraftServer getServer() {
		return serverRef.get();
	}

	public TournamentTeam get(PlayerTeam team) {
		if(team == null)
			return null;
		return teams.computeIfAbsent(team, t -> new TournamentTeam(t, TournamentPlayerManager.get(getServer())));
	}

	public List<TournamentTeam> getTeams() {
		return getServer().getScoreboard().getPlayerTeams().stream().map(this::get).toList();
	}
}
