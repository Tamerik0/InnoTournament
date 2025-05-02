package dev.necr0manthre.innotournament.tournament_events;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.necr0manthre.innotournament.tournament_events.parsing.TournamentEventHandlerParser;
import dev.necr0manthre.innotournament.tournament_events.parsing.TournamentEventParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventLoader {
	public static @NotNull Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> loadEvents(JsonElement json) {
		var map = new HashMap<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>>();
		for (var line : json.getAsJsonObject().entrySet()) {
			var event = TournamentEventParser.parse(line.getKey());
			if (line.getValue().isJsonPrimitive())
				map.put(event, Set.of(TournamentEventHandlerParser.parse(line.getValue().getAsString())));
			else {
				Set<ITournamentEventListener<?>> handlers = new HashSet<>();
				line.getValue().getAsJsonArray().forEach(e -> handlers.add(TournamentEventHandlerParser.parse(e.getAsString())));
				map.put(event, handlers);
			}
		}
		return map;
	}

	public static Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> loadEvents(Path path) {
		try {
			return loadEvents(JsonParser.parseReader(Files.newBufferedReader(path)));
		} catch (IOException e) {
			return Map.of();
		}
	}
}
