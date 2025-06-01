package dev.necr0manthre.innotournament.tournament_events;

import dev.necr0manthre.innotournament.tournament.Tournament;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LambdaTournamentEvent<T, V> extends AbstractTournamentEvent<T> {

	private final AtomicReference<V> reference = new AtomicReference<>();
	private final TriConsumer<Tournament, Consumer<T>, AtomicReference<V>> subscribeAction;
	private final TriConsumer<Tournament, Consumer<T>, V> removeAction;
	private Tournament tournament;
	private Consumer<T> callback;

	public LambdaTournamentEvent(TriConsumer<Tournament, Consumer<T>, AtomicReference<V>> subscribeAction, TriConsumer<Tournament, Consumer<T>, V> removeAction) {
		this.subscribeAction = subscribeAction;
		this.removeAction = removeAction;
	}

	@Override
	protected void subscribe(Tournament tournament, Consumer<T> callback) {
		this.tournament = tournament;
		this.callback = callback;
		subscribeAction.accept(tournament, callback, reference);
	}

	@Override
	public void remove() {
		removeAction.accept(tournament, callback, reference.get());
	}
}
