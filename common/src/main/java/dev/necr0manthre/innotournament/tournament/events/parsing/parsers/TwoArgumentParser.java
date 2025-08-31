package dev.necr0manthre.innotournament.tournament.events.parsing.parsers;

import dev.necr0manthre.innotournament.tournament.events.parsing.IParser;

import java.util.Optional;
import java.util.function.BiFunction;

public class TwoArgumentParser<T> implements IParser<String, T> {
	private final String name;
	private final BiFunction<String, String, T> eventFactory;

	public TwoArgumentParser(String name, BiFunction<String, String, T> eventFactory) {
		this.name = name;
		this.eventFactory = eventFactory;
	}

	@Override
	public Optional<T> tryParse(String s) {
		s = s.strip();
		if (s.startsWith(name + " ")) {
			var l = s.split(" ");
			if (l.length == 3)
				return Optional.of(eventFactory.apply(l[1], l[2]));
		}
		return Optional.empty();
	}
}
