package dev.necr0manthre.innotournament.tournament_events.parsing;

import dev.necr0manthre.innotournament.tournament_events.ITournamentEventListener;

public class TournamentEventHandlerParser {
	public static ITournamentEventListener<?> parse(Object data){
		return IParser.parse(TournamentEventHandlerParserRegistry.INSTANCE, data);
	}
}
