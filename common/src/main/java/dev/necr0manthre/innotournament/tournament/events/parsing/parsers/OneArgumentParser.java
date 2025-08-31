package dev.necr0manthre.innotournament.tournament.events.parsing.parsers;

import dev.necr0manthre.innotournament.tournament.events.parsing.IParser;

import java.util.Optional;
import java.util.function.Function;

public class OneArgumentParser<T> implements IParser<String, T> {
	private final String name;
	private final Function<String, T> eventFactory;
	private boolean greedy = false;

	public OneArgumentParser(String name, Function<String, T> eventFactory) {
		this.name = name;
		this.eventFactory = eventFactory;
	}
	public OneArgumentParser(String name, Function<String, T> eventFactory, boolean greedy) {
		this.name = name;
		this.eventFactory = eventFactory;
		this.greedy = greedy;
	}

	@Override
	public Optional<T> tryParse(String s) {
		s = s.strip();
		if (s.startsWith(name + " ")) {
			if(greedy) {
				var spacePos = s.indexOf(" ");
				return Optional.of(eventFactory.apply(s.substring(spacePos + 1)));
			}
			else {
				var l = s.split(" ");
				if(l.length == 2){
					return Optional.of(eventFactory.apply(l[1]));
				}
			}
		}
		return Optional.empty();
	}
}
