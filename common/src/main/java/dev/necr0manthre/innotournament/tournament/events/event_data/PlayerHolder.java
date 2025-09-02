package dev.necr0manthre.innotournament.tournament.events.event_data;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import lombok.Data;

@Data
public class PlayerHolder implements ISourcePlayerProvider, ITargetPlayerProvider, ITeamProvider {
    private final Entity player;

    @Override
    public Entity getSourcePlayer() {
        return player;
    }

    @Override
    public Entity getTargetPlayer() {
        return player;
    }

    @Override
    public Entity getTeam() {
        return TeamManager.getPlayersTeam(player).orElse(null);
    }
}
