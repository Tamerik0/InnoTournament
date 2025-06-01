package dev.necr0manthre.innotournament.tournament_events;

import dev.architectury.event.Event;

import java.util.function.Consumer;
import java.util.function.Function;

public class ArchitecturyEventBasedLambdaTournamentEvent<T, V> extends ArchitecturyEventBasedTournamentEvent<T, V> {
	private final Function<Consumer<V>, T> listenerFactory;

	public ArchitecturyEventBasedLambdaTournamentEvent(Event<T> event, Function<Consumer<V>, T> listenerFactory) {
		super(event);
		this.listenerFactory = listenerFactory;
	}

	@Override
	public T getListener(Consumer<V> callback) {
		return listenerFactory.apply(callback);
	}
}
