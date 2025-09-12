package dev.necr0manthre.innotournament.tournament.components;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.util.TournamentUtils;

public class TeamScore {
    public double score;

    public static TeamScore getScoreComponent(Entity team) {
        return TournamentUtils.getOrCreateEntityComponent(team, t -> new TeamScore(), TeamScore.class);
    }

    public static double getTeamScore(Entity team){
        return getScoreComponent(team).score;
    }

    public static void setTeamScore(Entity team, double score){
        getScoreComponent(team).score = score;
    }
}
