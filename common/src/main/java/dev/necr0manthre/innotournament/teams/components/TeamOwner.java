package dev.necr0manthre.innotournament.teams.components;

import dev.dominion.ecs.api.Entity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TeamOwner {
    public Entity player;

    public static TeamOwner getComponent(Entity team) {
        var component = team.get(TeamOwner.class);
        if (component == null) {
            component = new TeamOwner(null);
            team.add(component);
        }
        return component;
    }

    public static Entity getTeamOwner(Entity team) {
        return getComponent(team).player;
    }

    public static void setTeamOwner(Entity team, Entity player) {
        getComponent(team).player = player;
    }
}
