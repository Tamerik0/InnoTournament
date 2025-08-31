package dev.necr0manthre.innotournament.tournament.events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import lombok.Data;

@Data
public class PlayerKilled implements ISourcePlayerProvider, ITargetPlayerProvider, ITeamProvider{
	private final TournamentPlayer sourcePlayer;
	private final TournamentPlayer targetPlayer;
	@Override
	public TournamentPlayer getSourcePlayer() {
		return sourcePlayer;
	}

	@Override
	public TournamentPlayer getTargetPlayer() {
		return targetPlayer;
	}

	@Override
	public TournamentTeamManager.TournamentTeam getTeam() {
		return TournamentTeamManager.get(getSourcePlayer().getServerPlayer().get().server).getTeam(getSourcePlayer());
	}
}
