package dev.necr0manthre.innotournament.tournament.events.parsing;

import dev.necr0manthre.innotournament.tournament.events.ITournamentEventListener;
import dev.necr0manthre.innotournament.tournament.events.ParserRegistry;

public class TournamentEventHandlerParserRegistry extends ParserRegistry<ITournamentEventListener<?>> {
	public static final TournamentEventHandlerParserRegistry INSTANCE = new TournamentEventHandlerParserRegistry();
}
