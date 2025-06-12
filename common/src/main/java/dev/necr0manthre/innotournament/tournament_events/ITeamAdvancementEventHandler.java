package dev.necr0manthre.innotournament.tournament_events;

import dev.necr0manthre.innotournament.tournament_events.event_data.TeamAdvancement;

public interface ITeamAdvancementEventHandler {
	void handle(TeamAdvancement event);
}
