package dev.necr0manthre.innotournament.tournament_events.events;

import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.tournament.TournamentPlayerManager;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import dev.necr0manthre.innotournament.tournament_events.ArchitecturyEventBasedTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.event_data.ISourcePlayerProvider;
import dev.necr0manthre.innotournament.tournament_events.event_data.PlayerHolder;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class PlayerTickEvent extends ArchitecturyEventBasedTournamentEvent<TickEvent.Player, PlayerHolder> {
	private int counter = 0;
	private int period = 0;

	public PlayerTickEvent(int period) {
		super(TickEvent.PLAYER_POST);
		this.period = period;
	}

	@Override
	public TickEvent.Player getListener(Consumer<PlayerHolder> callback) {
		return player -> {
			if (player instanceof ServerPlayer serverPlayer && TournamentTeamManager.get(serverPlayer.server).getTeam(TournamentPlayerManager.getStatic(serverPlayer)) != null && counter++ % period == 0)
				callback.accept(new PlayerHolder(TournamentPlayerManager.getStatic(serverPlayer)));
		};
	}
}
