package dev.necr0manthre.innotournament.tournament.events.parsing;

import dev.necr0manthre.innotournament.tournament.events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament.events.ParserRegistry;

public class TournamentEventParserRegistry extends ParserRegistry<AbstractTournamentEvent<?>> {
	public static final TournamentEventParserRegistry INSTANCE = new TournamentEventParserRegistry();
}
