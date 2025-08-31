package dev.necr0manthre.innotournament.tournament.events;

import dev.necr0manthre.innotournament.tournament.events.event_data.TeamAdvancement;

public interface ITeamAdvancementEventHandler {
	void handle(TeamAdvancement event);
}
