package dev.necr0manthre.innotournament.init;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.TournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.event_data.ISourcePlayerProvider;
import dev.necr0manthre.innotournament.tournament_events.event_data.PlayerKilled;
import dev.necr0manthre.innotournament.tournament_events.events.AchievementEvent;
import dev.necr0manthre.innotournament.tournament_events.events.TimeEvent;
import dev.necr0manthre.innotournament.tournament_events.parsing.IParser;
import dev.necr0manthre.innotournament.tournament_events.parsing.TournamentEventParserRegistry;
import dev.necr0manthre.innotournament.tournament_events.parsing.parsers.OneArgumentParser;
import dev.necr0manthre.innotournament.tournament_events.parsing.parsers.SimpleDecoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public interface InnoTournamentEvents {
	IParser<String, TimeEvent> TIME = register(new OneArgumentParser<>(
			"time", time -> new TimeEvent(Long.parseLong(time))));
	IParser<String, AbstractTournamentEvent<?>> PREPARE = register(new SimpleDecoder<>("onPrepare", () -> new TournamentEvent<Object, Consumer<Object>>((tournament, callback, additionalData) -> tournament.onPrepare.register(callback), (tournament, callback, data) -> tournament.onStart.unregister(callback))));
	IParser<String, AbstractTournamentEvent<?>> START = register(new SimpleDecoder<>("onStart", () -> new TournamentEvent<Object, Consumer<Object>>((tournament, callback, additionalData) -> tournament.onStart.register(callback), (tournament, callback, data) -> tournament.onStart.unregister(callback))));
	IParser<String, AbstractTournamentEvent<?>> END = register(new SimpleDecoder<>("onEnd", () -> new TournamentEvent<Object, Consumer<Object>>((tournament, callback, additionalData) -> tournament.onEnd.register(callback), (tournament, callback, data) -> tournament.onStart.unregister(callback))));
	IParser<String, AbstractTournamentEvent<?>> ACHIEVEMENT = register(new OneArgumentParser<>("achievement", AchievementEvent::new));
	IParser<String, AbstractTournamentEvent<?>> PLAYER_KILLED = register(new SimpleDecoder<>("playerKilled", () -> new TournamentEvent<PlayerKilled, EntityEvent.LivingDeath>((tournament, callback, additionalData) -> {
		additionalData.set((entity, damageSource) -> {
			if (entity instanceof ServerPlayer target && damageSource.getEntity() instanceof ServerPlayer source)
				callback.accept(new PlayerKilled(source, target));
			return EventResult.pass();
		});
		EntityEvent.LIVING_DEATH.register(additionalData.get());
	}, (tournament, callback, data) -> EntityEvent.LIVING_DEATH.unregister(data))));

	private static <E extends AbstractTournamentEvent<?>, T extends IParser<String, E>> T register(T parser) {
		return TournamentEventParserRegistry.INSTANCE.register(String.class, parser);
	}

	static void init() {
	}
}
