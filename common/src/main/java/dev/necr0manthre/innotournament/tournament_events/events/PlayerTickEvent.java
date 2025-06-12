package dev.necr0manthre.innotournament.tournament_events.events;

import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.tournament.TournamentPlayerManager;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import dev.necr0manthre.innotournament.tournament_events.ArchitecturyEventBasedTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.event_data.ISourcePlayerProvider;
import dev.necr0manthre.innotournament.tournament_events.event_data.PlayerHolder;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerTickEvent extends ArchitecturyEventBasedTournamentEvent<TickEvent.Player, PlayerHolder> {
	private Map<UUID, Integer> counter = new HashMap<>();
	private int period = 0;

	public PlayerTickEvent(int period) {
		super(TickEvent.PLAYER_POST);
		this.period = period;
	}

	@Override
	public TickEvent.Player getListener(Consumer<PlayerHolder> callback) {
		return player -> {
			if (player instanceof ServerPlayer serverPlayer && TournamentTeamManager.get(serverPlayer.server).getTeam(TournamentPlayerManager.getStatic(serverPlayer)) != null && counter.computeIfAbsent(player.getUUID(), u -> 0
			) == 0) {
				callback.accept(new PlayerHolder(TournamentPlayerManager.getStatic(serverPlayer)));
			}
			counter.put(player.getUUID(), (counter.getOrDefault(player.getUUID(), 0) + 1) % period);

		};
	}
}
