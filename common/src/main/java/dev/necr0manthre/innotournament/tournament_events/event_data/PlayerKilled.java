package dev.necr0manthre.innotournament.tournament_events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentPlayer;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import lombok.Data;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@Data
public class PlayerKilled implements ISourcePlayerProvider, ITargetPlayerProvider, ITeamProvider{
	private final TournamentPlayer sourcePlayer;
	private final TournamentPlayer targetPlayer;
	@Override
	public TournamentPlayer getSourcePlayer() {
		return sourcePlayer;
	}

	@Override
	public TournamentPlayer getTargetPlayer() {
		return targetPlayer;
	}

	@Override
	public TournamentTeamManager.TournamentTeam getTeam() {
		return TournamentTeamManager.get(getSourcePlayer().getServerPlayer().get().server).getTeam(getSourcePlayer());
	}
}
