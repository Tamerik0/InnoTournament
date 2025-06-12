package dev.necr0manthre.innotournament.tournament_events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;

public interface ITeamProvider {
	TournamentTeamManager.TournamentTeam getTeam();
}
