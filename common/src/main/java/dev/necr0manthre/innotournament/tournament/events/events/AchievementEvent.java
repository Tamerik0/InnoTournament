package dev.necr0manthre.innotournament.tournament.events.events;

import dev.architectury.event.events.common.PlayerEvent;
import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament.TournamentPlayerManager;
import dev.necr0manthre.innotournament.tournament.events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament.events.event_data.PlayerHolder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class AchievementEvent extends AbstractTournamentEvent<PlayerHolder> {
	private final String achievement;
	private Consumer<PlayerHolder> callback;

	public AchievementEvent(String achievement) {
		this.achievement = achievement;
	}

	@Override
	protected void subscribe(Tournament tournament, Consumer<PlayerHolder> callback) {
		this.callback = callback;
		PlayerEvent.PLAYER_ADVANCEMENT.register(handler);
	}

	@Override
	public void remove() {
		PlayerEvent.PLAYER_ADVANCEMENT.unregister(handler);
	}

	PlayerEvent.PlayerAdvancement handler = this::handler;
	public void handler(ServerPlayer player, AdvancementHolder advancement) {
		if (advancement.id().toString().equals(achievement)) {
			callback.accept(new PlayerHolder(TournamentPlayerManager.getStatic(player)));
		}
	}
}
