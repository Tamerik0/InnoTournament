package dev.necr0manthre.innotournament.teams.events;

import dev.dominion.ecs.api.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PlayerTeamEvent {
    public final Entity player;
    public final Entity team;

    @Getter
    @Setter
    private boolean canceled = false;
}
