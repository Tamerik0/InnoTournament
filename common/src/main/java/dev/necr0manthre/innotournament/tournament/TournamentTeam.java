package dev.necr0manthre.innotournament.tournament;

import net.minecraft.world.scores.PlayerTeam;

import java.util.*;

public class TournamentTeam {
	public final PlayerTeam playerTeam;
	public final List<TournamentPlayer> requests = new ArrayList<>();
	private final Map<String, TournamentPlayer> members = new HashMap<>();
	public int score = 0;
	public boolean acceptAll = false;
	public TournamentPlayer owner = null;

	public TournamentTeam(PlayerTeam playerTeam, TournamentPlayerManager playerManager) {
		this.playerTeam = playerTeam;
		var nameSet = new HashSet<>(playerTeam.getPlayers());
		for (var player : playerManager.getPlayers()) {
			if (nameSet.contains(player.player.getScoreboardName()))
				members.put(player.player.getScoreboardName(), player);
		}
	}

	public TournamentTeam(PlayerTeam playerTeam, TournamentPlayerManager playerManager, TournamentPlayer owner) {
		this(playerTeam, playerManager);
		this.owner = owner;
	}

	public boolean removePlayer(TournamentPlayer player) {
		if (!members.containsKey(player.player.getScoreboardName()))
			return false;
		playerTeam.getScoreboard().removePlayerFromTeam(player.player.getScoreboardName(), playerTeam);
		members.remove(player.player.getScoreboardName());
		if (Objects.equals(owner, player.player.getScoreboardName())) {
			if (playerTeam.getPlayers().isEmpty()) {
				owner = null;
				acceptAll = true;
			}
			owner = members.values().iterator().next();
		}
		return true;
	}

	public boolean addPlayer(TournamentPlayer player) {
		if (members.containsKey(player.player.getScoreboardName()))
			return false;
		playerTeam.getScoreboard().addPlayerToTeam(player.player.getScoreboardName(), playerTeam);
		members.put(player.player.getScoreboardName(), player);
		return true;
	}

	public Set<String> getPlayersScoreboardNames() {
		return members.keySet();
	}

	public void requestJoin(TournamentPlayer player) {
		if (requests.contains(player)) return;
		requests.add(player);
	}

	public List<TournamentPlayer> getPlayers(TournamentPlayerManager playerManager) {
		return members.values().stream().toList();
	}

}
