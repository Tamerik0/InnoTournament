package dev.necr0manthre.innotournament;

import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import net.minecraft.network.chat.Component;

import java.util.Objects;
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

    public static Component getFullTeamName(Entity team) {
        var displayName = TeamManager.getPlayerTeam(team).getDisplayName();
        var name = TeamManager.getPlayerTeam(team).getName();
        if (Objects.equals(displayName.getString(), name)) {
            return displayName;
        }
        return Component.empty().append(displayName).append(" (%s)".formatted(name));
    }
}
