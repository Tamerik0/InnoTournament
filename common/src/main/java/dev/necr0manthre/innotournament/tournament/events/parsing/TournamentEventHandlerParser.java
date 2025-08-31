package dev.necr0manthre.innotournament.tournament.events.parsing;

import dev.necr0manthre.innotournament.tournament.events.ITournamentEventListener;

public class TournamentEventHandlerParser {
	public static ITournamentEventListener<?> parse(Object data){
		return IParser.parse(TournamentEventHandlerParserRegistry.INSTANCE, data);
	}
}
