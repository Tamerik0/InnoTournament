package dev.necr0manthre.innotournament.teams.components;

import dev.dominion.ecs.api.Entity;

public class TeamSettings {
    public boolean acceptAll;

    public static TeamSettings getSettings(Entity team) {
        var component = team.get(TeamSettings.class);
        if (component == null) {
            component = new TeamSettings();
            team.add(component);
        }
        return component;
    }
}
