package dev.necr0manthre.innotournament.teams;

import dev.dominion.ecs.api.Entity;

import java.util.Optional;

public interface TeamUtils {
    static Optional<Entity> getPlayersTeam(Entity player) {
        return Optional.empty();
    }
}
