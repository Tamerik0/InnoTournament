package dev.necr0manthre.innotournament.tournament.components;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.util.TournamentUtils;
import net.minecraft.advancements.AdvancementHolder;

import java.util.HashSet;
import java.util.Set;

public class TeamAdvancements {
    public final Set<AdvancementHolder> advancements = new HashSet<>();

    public static TeamAdvancements get(Entity team) {
        return TournamentUtils.getOrCreateEntityComponent(team, t -> new TeamAdvancements(), TeamAdvancements.class);
    }
}
