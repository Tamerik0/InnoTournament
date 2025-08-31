package dev.necr0manthre.innotournament.tournament.events;

import dev.necr0manthre.innotournament.tournament.events.parsing.IParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParserRegistry<O> {
	private final Map<Class<?>, Set<IParser<?, ? extends O>>> registry = new HashMap<>();

	public <T, E extends O, P extends IParser<T, E>> P register(Class<T> clazz, P parser) {
		registry.computeIfAbsent(clazz, k -> new HashSet<>()).add(parser);
		return parser;
	}

	public <T> Set<IParser<T, ? extends O>> getParsers(Class<T> clazz) {
		return (Set<IParser<T, ? extends O>>) (Object) registry.computeIfAbsent(clazz, k -> new HashSet<>());
	}
}
