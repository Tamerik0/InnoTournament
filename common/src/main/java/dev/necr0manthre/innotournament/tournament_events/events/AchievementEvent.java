package dev.necr0manthre.innotournament.tournament_events.events;

import dev.architectury.event.events.common.PlayerEvent;
import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament.TournamentPlayerManager;
import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.event_data.ISourcePlayerProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class AchievementEvent extends AbstractTournamentEvent<ISourcePlayerProvider> {
	private final String achievement;
	private Tournament tournament;
	private Consumer<ISourcePlayerProvider> callback;

	public AchievementEvent(String achievement) {
		this.achievement = achievement;
	}

	@Override
	protected void subscribe(Tournament tournament, Consumer<ISourcePlayerProvider> callback) {
		this.tournament = tournament;
		this.callback = callback;
		PlayerEvent.PLAYER_ADVANCEMENT.register(this::handler);
	}

	@Override
	public void remove() {
		PlayerEvent.PLAYER_ADVANCEMENT.unregister(this::handler);
	}

	public void handler(ServerPlayer player, AdvancementHolder advancement) {
		if (advancement.id().toString().equals(achievement)) {
			callback.accept(() -> TournamentPlayerManager.getStatic(player));
		}
	}
}
