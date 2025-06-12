package dev.necr0manthre.innotournament.tournament;

import lombok.Getter;

public class Invite {
	@Getter
	private final TournamentPlayer player;
	@Getter
	private final TournamentTeamManager.TournamentTeam team;
	@Getter
	private int lifetime;

	public Invite(TournamentPlayer player, TournamentTeamManager.TournamentTeam team, int lifetime) {
		this.player = player;
		this.team = team;
		this.lifetime = lifetime;
	}

	public boolean isValid() {
		return player.player != null && lifetime > 0 && team != null && team.getPlayerTeam() != null;
	}

	public boolean tick() {
		lifetime--;
		return !isValid();
	}
}
