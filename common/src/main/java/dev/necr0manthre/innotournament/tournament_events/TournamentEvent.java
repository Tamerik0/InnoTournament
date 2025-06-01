package dev.necr0manthre.innotournament.tournament_events;

import dev.necr0manthre.innotournament.tournament.Tournament;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class TournamentEvent<T, V> extends AbstractTournamentEvent<T> {
	private Tournament tournament;
	private Consumer<T> callback;
	private V huy;

	@Override
	protected void subscribe(Tournament tournament, Consumer<T> callback) {
		this.tournament = tournament;
		this.callback = callback;
		huy = subscribeInternal(tournament, callback);
	}

	@Override
	public void remove() {
		removeInternal(tournament, callback, huy);
	}

	protected abstract V subscribeInternal(Tournament tournament, Consumer<T> callback);

	protected abstract void removeInternal(Tournament tournament, Consumer<T> callback, V obj);
}
