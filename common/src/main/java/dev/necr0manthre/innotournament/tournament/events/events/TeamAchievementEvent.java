package dev.necr0manthre.innotournament.tournament.events.events;

import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament.events.ITeamAdvancementEventHandler;
import dev.necr0manthre.innotournament.tournament.events.TournamentEvent;
import dev.necr0manthre.innotournament.tournament.events.event_data.TeamAdvancement;

import java.util.function.Consumer;

public class TeamAchievementEvent extends TournamentEvent<TeamAdvancement, ITeamAdvancementEventHandler> {
	private final String achievement;

	public TeamAchievementEvent(String achievement) {
		this.achievement = achievement;
	}

	@Override
	protected ITeamAdvancementEventHandler subscribeInternal(Tournament tournament, Consumer<TeamAdvancement> callback) {
		ITeamAdvancementEventHandler handler = event -> {
			if(event.getAdvancement().id().toString().equals(achievement)){
				callback.accept(event);
			}
		};
		tournament.advancementHandler.teamAdvancementEvent.register(handler);
		return handler;
	}

	@Override
	protected void removeInternal(Tournament tournament, Consumer<TeamAdvancement> callback, ITeamAdvancementEventHandler obj) {
		tournament.advancementHandler.teamAdvancementEvent.unregister(obj);
	}
}
