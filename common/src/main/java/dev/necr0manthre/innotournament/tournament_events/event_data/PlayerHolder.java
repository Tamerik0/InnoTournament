package dev.necr0manthre.innotournament.tournament_events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.TournamentTeam;
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
	public TournamentTeam getTeam() {
		return player.tournament.get().getTeamManager().getTeam(player);
	}
}
