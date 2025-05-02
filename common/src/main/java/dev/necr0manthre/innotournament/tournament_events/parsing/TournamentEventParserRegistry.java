package dev.necr0manthre.innotournament.tournament_events.parsing;

import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.ParserRegistry;

public class TournamentEventParserRegistry extends ParserRegistry<AbstractTournamentEvent<?>> {
	public static final TournamentEventParserRegistry INSTANCE = new TournamentEventParserRegistry();
}
