package dev.necr0manthre.innotournament.tournament.components;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.TournamentUtils;
import dev.necr0manthre.innotournament.tournament.Tournament;

import java.lang.ref.WeakReference;

public class TournamentPlayer {
    public int lives;
    public WeakReference<Tournament> tournament;

    public static TournamentPlayer getTournamentData(Entity player) {
        return TournamentUtils.getOrCreateEntityComponent(player, e -> new TournamentPlayer(), TournamentPlayer.class);
    }
}
