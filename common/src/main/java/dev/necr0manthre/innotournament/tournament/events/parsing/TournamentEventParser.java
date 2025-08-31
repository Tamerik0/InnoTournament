package dev.necr0manthre.innotournament.tournament.events.parsing;

import dev.necr0manthre.innotournament.tournament.events.AbstractTournamentEvent;

public class TournamentEventParser {
	public static AbstractTournamentEvent<?> parse(Object data){
		return IParser.parse(TournamentEventParserRegistry.INSTANCE, data);
	}
}
