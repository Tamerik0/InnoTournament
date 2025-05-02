package dev.necr0manthre.innotournament.tournament_events.parsing;

import dev.necr0manthre.innotournament.tournament_events.ITournamentEventListener;
import dev.necr0manthre.innotournament.tournament_events.ParserRegistry;

public class TournamentEventHandlerParserRegistry extends ParserRegistry<ITournamentEventListener<?>> {
	public static final TournamentEventHandlerParserRegistry INSTANCE = new TournamentEventHandlerParserRegistry();
}
