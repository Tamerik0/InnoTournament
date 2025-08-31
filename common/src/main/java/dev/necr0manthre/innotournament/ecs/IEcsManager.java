package dev.necr0manthre.innotournament.ecs;

import dev.dominion.ecs.api.Entity;
import org.jetbrains.annotations.NotNull;

public interface IEcsManager<T> {
    Entity getEntity(T object);

    default  <V> V getComponent(@NotNull T object, Class<V> componentClass) {
        return getEntity(object).get(componentClass);
    }
}
