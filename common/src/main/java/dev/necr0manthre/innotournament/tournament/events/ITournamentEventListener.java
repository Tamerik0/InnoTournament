package dev.necr0manthre.innotournament.tournament.events;

import dev.necr0manthre.innotournament.tournament.Tournament;

public interface ITournamentEventListener<T> {
	void listen(Tournament tournament, T value);
}
