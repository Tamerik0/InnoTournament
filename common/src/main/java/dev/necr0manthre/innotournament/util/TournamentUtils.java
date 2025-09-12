package dev.necr0manthre.innotournament.util;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import dev.necr0manthre.innotournament.tournament.components.TeamScore;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class TournamentUtils {
    public static <T> T getOrCreateEntityComponent(Entity entity, Function<Entity, T> factory, Class<T> tClass) {
        var data = entity.get(tClass);
        if (data == null) {
            data = factory.apply(entity);
            entity.add(data);
            return data;
        }
        return data;
    }

    public static boolean changeTeamName(Entity team, Component name) {
        if (!TeamManager.changeName(team, name.getString()))
            return false;
        TeamManager.getPlayerTeam(team).setDisplayName(name);
        return true;
    }

    public static String formatTeamScore(double score) {
        return String.format("%.2f", score);
    }

    public static String formatTeamScore(Entity team) {
        return String.format("%.2f", TeamScore.getTeamScore(team));
    }
}
