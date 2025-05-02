package dev.necr0manthre.innotournament.tournament_events.parsing.parsers;

import dev.necr0manthre.innotournament.tournament_events.parsing.IParser;

import java.util.Optional;
import java.util.function.Supplier;

public class SimpleDecoder<T> implements IParser<String, T> {
	private final String name;
	private final Supplier<T> eventFactory;

	public SimpleDecoder(String name, Supplier<T> eventFactory) {
		this.name = name;
		this.eventFactory = eventFactory;
	}

	@Override
	public Optional<T> tryParse(String s) {
		if (s.strip().equals(name)) return Optional.of(eventFactory.get());
		return Optional.empty();
	}
}
