package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;

public class TournamentAdvancementHandler {
	private final WeakReference<Tournament> tournament;

	public TournamentAdvancementHandler(Tournament tournament) {
		this.tournament = new WeakReference<>(tournament);
	}

	public void register() {
		PlayerEvent.PLAYER_ADVANCEMENT.register(this::handle);
	}

	private void handle(ServerPlayer serverPlayer, AdvancementHolder advancement) {
		var player = tournament.get().getPlayerManager().get(serverPlayer);
		var team = tournament.get().getTeamManager().getTeam(player);
		if (team != null) {
			team.makeAdvancement(advancement);
		}
	}

	public void unregister() {
		PlayerEvent.PLAYER_ADVANCEMENT.unregister(this::handle);
	}
}
