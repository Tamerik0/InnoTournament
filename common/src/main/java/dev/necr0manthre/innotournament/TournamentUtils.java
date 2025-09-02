package dev.necr0manthre.innotournament;

import dev.dominion.ecs.api.Entity;

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
}
