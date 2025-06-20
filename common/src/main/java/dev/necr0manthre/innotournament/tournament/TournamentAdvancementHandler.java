package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.PlayerEvent;
import dev.necr0manthre.innotournament.tournament_events.ITeamAdvancementEventHandler;
import dev.necr0manthre.innotournament.tournament_events.event_data.TeamAdvancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;

public class TournamentAdvancementHandler {
	private final WeakReference<Tournament> tournament;
	public final Event<ITeamAdvancementEventHandler> teamAdvancementEvent = EventFactory.createLoop();

	public TournamentAdvancementHandler(Tournament tournament) {
		this.tournament = new WeakReference<>(tournament);
	}

	public void register() {
		PlayerEvent.PLAYER_ADVANCEMENT.register(handle);
	}

	PlayerEvent.PlayerAdvancement handle = this::handle;

	private void handle(ServerPlayer serverPlayer, AdvancementHolder advancement) {
		var player = tournament.get().getPlayerManager().get(serverPlayer);
		var team = tournament.get().getTeamManager().getTeam(player);
		if (team != null && !team.hasAdvancement(advancement)) {
			team.makeAdvancement(advancement);
			teamAdvancementEvent.invoker().handle(new TeamAdvancement(team, advancement));
		}
	}

	public void unregister() {
		PlayerEvent.PLAYER_ADVANCEMENT.unregister(handle);
	}
}
