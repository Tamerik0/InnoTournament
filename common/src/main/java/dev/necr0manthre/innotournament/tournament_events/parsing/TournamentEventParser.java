package dev.necr0manthre.innotournament.tournament_events.parsing;

import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;

public class TournamentEventParser {
	public static AbstractTournamentEvent<?> parse(Object data){
		return IParser.parse(TournamentEventParserRegistry.INSTANCE, data);
	}
}
