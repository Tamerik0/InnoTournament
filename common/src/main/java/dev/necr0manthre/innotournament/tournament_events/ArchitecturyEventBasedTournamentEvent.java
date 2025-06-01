package dev.necr0manthre.innotournament.tournament_events;

import dev.architectury.event.Event;
import dev.necr0manthre.innotournament.tournament.Tournament;

import java.util.function.Consumer;

public abstract class ArchitecturyEventBasedTournamentEvent<T,V> extends TournamentEvent<V, T>{
	private final Event<T> event;
	public ArchitecturyEventBasedTournamentEvent(Event<T> event){
		this.event = event;
	}
	public abstract T getListener(Consumer<V> callback);
	@Override
	protected T subscribeInternal(Tournament tournament, Consumer<V> callback) {
		var listener = getListener(callback);
		event.register(listener);
		return listener;
	}

	@Override
	protected void removeInternal(Tournament tournament, Consumer<V> callback, T obj) {
		event.unregister(obj);
	}
}
