package dev.necr0manthre.innotournament.teams.invites;

import dev.dominion.ecs.api.Entity;

import java.util.HashSet;
import java.util.Set;

public class Invites {
    public final Set<Invite> invites = new HashSet<>();

    public static Invites getComponent(Entity team) {
        var component = team.get(Invites.class);
        if (component == null) {
            component = new Invites();
            team.add(component);
        }
        return component;
    }
}
