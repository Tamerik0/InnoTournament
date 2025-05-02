package dev.necr0manthre.innotournament.tournament_events.event_data;

import lombok.Data;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@Data
public class PlayerKilled implements ISourcePlayerProvider, ITargetPlayerProvider{
	private final ServerPlayer sourcePlayer;
	private final ServerPlayer targetPlayer;
	@Override
	public ServerPlayer getSourcePlayer() {
		return sourcePlayer;
	}

	@Override
	public ServerPlayer getTargetPlayer() {
		return targetPlayer;
	}
}
