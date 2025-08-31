package dev.necr0manthre.innotournament.tournament.events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import lombok.Data;

@Data
public class PlayerHolder implements ISourcePlayerProvider, ITargetPlayerProvider, ITeamProvider{
	private final TournamentPlayer player;

	@Override
	public TournamentPlayer getSourcePlayer() {
		return player;
	}

	@Override
	public TournamentPlayer getTargetPlayer() {
		return player;
	}

	@Override
	public TournamentTeamManager.TournamentTeam getTeam() {
		return player.tournament.get().getTeamManager().getTeam(player);
	}
}
