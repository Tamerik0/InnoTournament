package dev.necr0manthre.innotournament.tournament_events;

import dev.necr0manthre.innotournament.tournament.Tournament;

public interface ITournamentEventListener<T> {
	void listen(Tournament tournament, T value);
}
