package dev.necr0manthre.innotournament.tournament.events;

import dev.necr0manthre.innotournament.tournament.Tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractTournamentEvent<T> {
	private final List<ITournamentEventListener<T>> listeners = new ArrayList<>();

	public void addListener(ITournamentEventListener<T> listener) {
		listeners.add(listener);
	}

	public void removeListener(ITournamentEventListener<T> listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(Tournament tournament, T value) {
		listeners.forEach(listener -> listener.listen(tournament, value));
	}

	public void subscribe(Tournament tournament) {
		subscribe(tournament, event -> notifyListeners(tournament, event));
	}

	protected abstract void subscribe(Tournament tournament, Consumer<T> callback);

	public abstract void remove();
}
