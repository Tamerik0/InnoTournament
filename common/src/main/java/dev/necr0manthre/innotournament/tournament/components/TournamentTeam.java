package dev.necr0manthre.innotournament.tournament.components;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.TournamentUtils;
import dev.necr0manthre.innotournament.tournament.Tournament;

import java.lang.ref.WeakReference;

public class TournamentTeam {
    public WeakReference<Tournament> tournament;
    public static TournamentTeam get(Entity team){
        return TournamentUtils.getOrCreateEntityComponent(team, t -> new TournamentTeam(), TournamentTeam.class);
    }
}
