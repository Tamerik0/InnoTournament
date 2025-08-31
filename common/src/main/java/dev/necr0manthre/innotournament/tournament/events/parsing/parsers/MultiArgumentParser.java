package dev.necr0manthre.innotournament.tournament.events.parsing.parsers;

import dev.necr0manthre.innotournament.tournament.events.parsing.IParser;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class MultiArgumentParser<T> implements IParser<String, T> {
	private final String name;
	private final Function<String[], T> eventFactory;

	public MultiArgumentParser(String name, Function<String[], T> eventFactory) {
		this.name = name;
		this.eventFactory = eventFactory;
	}

	@Override
	public Optional<T> tryParse(String s) {
		s = s.strip();
		if (s.startsWith(name + " ")) {
			var l = s.split(" ");
			return Optional.of(eventFactory.apply(Arrays.stream(l).skip(1).toArray(String[]::new)));
		}
		return Optional.empty();
	}
}
