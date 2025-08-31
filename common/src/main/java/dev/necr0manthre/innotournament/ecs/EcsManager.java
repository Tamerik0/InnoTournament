package dev.necr0manthre.innotournament.ecs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
public class EcsManager<T> implements IEcsManager<T>{
    public final Dominion ecs = Dominion.create();
    private final BiMap<T, Entity> entityObjectMap = HashBiMap.create();
    private final BiConsumer<T, Entity> onCreate;

    public EcsManager(BiConsumer<T, Entity> onCreate) {
        this.onCreate = onCreate;
    }

    public T getObject(@NotNull Entity entity) {
        var obj = entityObjectMap.inverse().get(entity);
        if(obj == null)
            throw new IllegalArgumentException("Entity is not mapped to an object");
        return obj;
    }

    public Entity getEntity(T object) {
        return entityObjectMap.computeIfAbsent(object, o ->{
            var entity = ecs.createEntity();
            onCreate.accept(o, entity);
            return entity;
        });
    }

    @Nullable
    public <V> V getComponent(@NotNull T object, Class<V> componentClass) {
        var entity = getEntity(object);
        return entity.get(componentClass);
    }

    public void remove() {
        ecs.close();
    }
}
