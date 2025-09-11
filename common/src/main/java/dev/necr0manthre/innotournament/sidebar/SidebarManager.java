package dev.necr0manthre.innotournament.sidebar;

import dev.architectury.event.events.common.PlayerEvent;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.SidebarInterface;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class SidebarManager {
    @Getter
    private final MinecraftServer server;
    private final Map<UUID, SuppliedSidebar> sidebars = new HashMap<>();
    @Setter
    private Function<ServerPlayer, SuppliedSidebarData> sidebarFactory;
    PlayerEvent.PlayerJoin onPlayerJoin = this::onPlayerJoin;

    public SidebarManager(MinecraftServer server, Function<ServerPlayer, SuppliedSidebarData> sidebarFactory) {
        this.server = server;
        this.sidebarFactory = sidebarFactory;
        PlayerEvent.PLAYER_JOIN.register(onPlayerJoin);
        for (var player : server.getPlayerList().getPlayers())
            onPlayerJoin(player);
    }

    public void updateSidebarFactory(Function<ServerPlayer, SuppliedSidebarData> sidebarFactory) {
        this.sidebarFactory = sidebarFactory;
        for (var sidebar : sidebars.values()) {
            for (var player : sidebar.getPlayerHandlerSet())
                sidebar.removePlayer(player);
        }
        sidebars.replaceAll((p, v) -> createSidebar(p));
    }

    public void setPlayerSidebarSupplier(UUID uuid, Supplier<SidebarInterface.SidebarData> supplier) {
        getSidebarFor(uuid).setSupplier(supplier);
    }

    public void setPlayerSidebarSupplier(ServerPlayer player, Supplier<SidebarInterface.SidebarData> supplier) {
        setPlayerSidebarSupplier(player.getUUID(), supplier);
    }

    public void updateSidebar(UUID uuid) {
        getSidebarFor(uuid).setDirty(true);
    }

    public void updateSidebar(ServerPlayer player) {
        updateSidebar(player.getUUID());
    }

    public void setSideBarPriority(UUID uuid, Sidebar.Priority priority) {
        getSidebarFor(uuid).setPriority(priority);
    }

    public void setSideBarPriority(ServerPlayer player, Sidebar.Priority priority) {
        setSideBarPriority(player.getUUID(), priority);
    }


    public void setSidebarUpdateRate(UUID uuid, int rate) {
        getSidebarFor(uuid).setUpdateRate(rate);
    }

    public void setSidebarUpdateRate(ServerPlayer player, int rate) {
        setSidebarUpdateRate(player.getUUID(), rate);
    }

    public SuppliedSidebar getSidebarFor(UUID uuid) {
        return sidebars.computeIfAbsent(uuid, this::createSidebar);
    }

    public SuppliedSidebar getSidebarFor(ServerPlayer player) {
        return getSidebarFor(player.getUUID());
    }

    private SuppliedSidebar createSidebar(UUID uuid) {
        var sidebar = new SuppliedSidebar();
        var player = server.getPlayerList().getPlayer(uuid);
        assert player != null;
        sidebar.addPlayer(player);
        var data = sidebarFactory.apply(player);
        sidebar.setUpdateRate(data.updateRate);
        sidebar.setPriority(data.priority);
        sidebar.setSupplier(data.supplier);
        return sidebar;
    }

    private void onPlayerJoin(ServerPlayer player) {
        getSidebarFor(player).addPlayer(player);
        getSidebarFor(player).show();
    }

    public void remove() {
        for (var sidebar : sidebars.values()) {
            for (var player : sidebar.getPlayerHandlerSet())
                sidebar.removePlayer(player);
        }
        PlayerEvent.PLAYER_JOIN.unregister(onPlayerJoin);
    }

    public record SuppliedSidebarData(Sidebar.Priority priority, int updateRate,
                                      Supplier<SidebarInterface.SidebarData> supplier) {
    }
}
