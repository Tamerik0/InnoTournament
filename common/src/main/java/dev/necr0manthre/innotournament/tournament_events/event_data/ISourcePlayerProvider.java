package dev.necr0manthre.innotournament.tournament_events.event_data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface ISourcePlayerProvider {
	ServerPlayer getSourcePlayer();
}
