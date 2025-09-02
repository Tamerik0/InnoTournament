package dev.necr0manthre.innotournament.init;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.tournament.components.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.events.ITournamentEventListener;
import dev.necr0manthre.innotournament.tournament.events.event_data.ISourcePlayerProvider;
import dev.necr0manthre.innotournament.tournament.events.event_data.ITeamProvider;
import dev.necr0manthre.innotournament.tournament.events.handlers.WorldBorderAction;
import dev.necr0manthre.innotournament.tournament.events.parsing.IParser;
import dev.necr0manthre.innotournament.tournament.events.parsing.TournamentEventHandlerParserRegistry;
import dev.necr0manthre.innotournament.tournament.events.parsing.parsers.MultiArgumentParser;
import dev.necr0manthre.innotournament.tournament.events.parsing.parsers.OneArgumentParser;
import dev.necr0manthre.innotournament.tournament.events.parsing.parsers.SimpleDecoder;
import dev.necr0manthre.innotournament.tournament.events.parsing.parsers.TwoArgumentParser;

public interface InnoTournamentEventHandlers {
	IParser<String, ITournamentEventListener<Object>> WORLD_BORDER = register(new OneArgumentParser<>("world_border", size -> new WorldBorderAction(Double.parseDouble(size), 0)));
	IParser<String, ITournamentEventListener<Object>> WORLD_BORDER_TIME = register(new TwoArgumentParser<>("world_border", (size, time) -> new WorldBorderAction(Double.parseDouble(size), Long.parseLong(time))));
	IParser<String, ITournamentEventListener<Object>> LOG_ARG = register(new OneArgumentParser<>("log", str -> (tournament, obj) -> Innotournament.LOGGER.info(str)));
	IParser<String, ITournamentEventListener<Object>> LOG_EVENT = register(new SimpleDecoder<>("log", () -> (tournament, obj) -> Innotournament.LOGGER.info(obj.toString())));
	IParser<String, ITournamentEventListener<Object>> PLACE_START_BOX = register(new SimpleDecoder<>("place_start_box", () -> (tournament, obj) -> tournament.placeStartBox()));
	IParser<String, ITournamentEventListener<Object>> REMOVE_START_BOX = register(new SimpleDecoder<>("remove_start_box", () -> (tournament, obj) -> tournament.removeStartBox()));
	IParser<String, ITournamentEventListener<ITeamProvider>> SCORE = register(new OneArgumentParser<>("add_score", score -> (tournament, teamProvider) -> tournament.addScores(teamProvider.getTeam(), Integer.parseInt(score))));
	IParser<String, ITournamentEventListener<ISourcePlayerProvider>> SCORE_FROM_LIVES = register(new MultiArgumentParser<>("add_score_by_lives", scores -> (tournament, playerProvider) -> tournament.addScores(playerProvider.getSourcePlayer(), Integer.parseInt(scores[TournamentPlayer.getTournamentData(playerProvider.getSourcePlayer()).lives]))));
	IParser<String, ITournamentEventListener<Object>> PVP = register(new OneArgumentParser<>("pvp", enabled -> (tournament, obj) -> tournament.getServer().setPvpAllowed(Boolean.parseBoolean(enabled))));
	IParser<String, ITournamentEventListener<Object>> EXECUTE_COMMAND = register(new OneArgumentParser<>("execute_command", cmd -> (tournament, obj) -> {
		try {
			tournament.getServer().getCommands().getDispatcher().execute(cmd, tournament.getServer().createCommandSourceStack().withPermission(2));
		} catch (CommandSyntaxException e) {
			Innotournament.LOGGER.error("Failed to execute command: {}", cmd, e);
		}
	}, true));

	private static <E extends ITournamentEventListener<?>, T extends IParser<String, E>> T register(T parser) {
		return TournamentEventHandlerParserRegistry.INSTANCE.register(String.class, parser);
	}

	static void init() {
	}
}
