package dev.necr0manthre.innotournament.teams.invites;

import dev.dominion.ecs.api.Entity;
import lombok.Getter;

public class Invite {
    public final Entity team;
    public final Entity player;
    @Getter
    private int lifetime;

    public Invite(Entity team, Entity player, int lifetime) {
        this.team = team;
        this.player = player;
        this.lifetime = lifetime;
    }

    public void tick() {
        if (lifetime > 0) {
            lifetime--;
        }
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }
}
