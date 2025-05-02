package dev.necr0manthre.innotournament.tournament_events.parsing;

import dev.necr0manthre.innotournament.tournament_events.ParserRegistry;

import java.util.Optional;

public interface IParser<T, E> {
	Optional<E> tryParse(T t);
	static <V, I> V parse(ParserRegistry<V> registry, I input){
		for(var parser:registry.getParsers((Class<I>)input.getClass())){
			var parsed = parser.tryParse(input);
			if(parsed.isPresent())
				return parsed.get();
		}
		return null;
	}
}
