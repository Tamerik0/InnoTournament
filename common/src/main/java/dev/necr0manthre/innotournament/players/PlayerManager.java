package dev.necr0manthre.innotournament.players;

import com.mojang.authlib.GameProfile;
import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.ecs.EcsManager;
import dev.necr0manthre.innotournament.util.ServerBoundObjManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

@MethodsReturnNonnullByDefault
public class PlayerManager implements ServerBoundObjManager.Removable {
    private static final ServerBoundObjManager<PlayerManager> managerMap = new ServerBoundObjManager<>();
    private final WeakReference<MinecraftServer> serverRef;
    private final EcsManager<UUID> ecsManager = new EcsManager<>((uuid, entity) -> entityManagerMap.put(entity, this));
    private static final Map<Entity, PlayerManager> entityManagerMap = new WeakHashMap<>();

    public PlayerManager(MinecraftServer server) {
        this.serverRef = new WeakReference<>(server);
    }

    public MinecraftServer getServer() {
        return Objects.requireNonNull(serverRef.get());
    }

    public static PlayerManager get(MinecraftServer server) {
        return managerMap.computeIfAbsent(server, PlayerManager::new);
    }

    public static PlayerManager get(Entity player) {
        return Objects.requireNonNull(entityManagerMap.get(player));
    }

    public Entity getEntity(UUID uuid) {
        return ecsManager.getEntity(uuid);
    }

    public Optional<Entity> getEntity(String name) {
        var profile = Optional.ofNullable(getServer().getProfileCache()).map(cache -> cache.get(name)).orElseGet(() -> getServer().getProfileRepository().findProfileByName(name));
        return profile.map(p -> getEntity(p.getId()));
    }

    public static Entity getEntity(ServerPlayer player) {
        return get(player.server).getEntity(player.getUUID());
    }

    public static UUID getUUID(@NotNull Entity player) {
        return get(player).ecsManager.getObject(player);
    }

    public static Optional<GameProfile> getGameProfile(Entity player) {
        return Optional.ofNullable(get(player).getServer().getProfileCache()).flatMap(cache -> cache.get(getUUID(player)));
    }

    public static Optional<String> getName(Entity player) {
        return getGameProfile(player).map(GameProfile::getName);
    }

    public static Optional<Component> getDisplayName(Entity player) {
        return getGameProfile(player).map(profile -> {
            var name = profile.getName();
            return PlayerTeam.formatNameForTeam(get(player).getServer().getScoreboard().getPlayerTeam(name), Component.literal(name))
                    .withStyle((style) -> style.withClickEvent(new ClickEvent.SuggestCommand("/tell " + name + " "))
                            .withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, profile.getId(), Component.literal(name))))
                            .withInsertion(name));
        });
    }

    public static Optional<ServerPlayer> getServerPlayer(@NotNull Entity player) {
        return Optional.ofNullable(get(player).getServer().getPlayerList().getPlayer(getUUID(player)));
    }

    public static void register() {
        managerMap.register();
    }

    public static void unregister() {
        managerMap.unregister();
    }

    public void remove() {
        ecsManager.remove();
    }

    @Override
    public void onRemove() {
        remove();
    }
}
