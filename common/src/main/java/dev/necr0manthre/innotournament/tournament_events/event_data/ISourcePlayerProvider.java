package dev.necr0manthre.innotournament.tournament_events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface ISourcePlayerProvider {
	TournamentPlayer getSourcePlayer();
}
