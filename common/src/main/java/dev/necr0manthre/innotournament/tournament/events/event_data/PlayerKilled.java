package dev.necr0manthre.innotournament.tournament.events.event_data;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import lombok.Data;

@Data
public class PlayerKilled implements ISourcePlayerProvider, ITargetPlayerProvider, ITeamProvider {
    private final Entity sourcePlayer;
    private final Entity targetPlayer;

    @Override
    public Entity getSourcePlayer() {
        return sourcePlayer;
    }

    @Override
    public Entity getTargetPlayer() {
        return targetPlayer;
    }

    @Override
    public Entity getTeam() {
        return TeamManager.getPlayersTeam(getSourcePlayer()).orElse(null);
    }
}
