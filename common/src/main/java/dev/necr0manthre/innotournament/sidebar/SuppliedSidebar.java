package dev.necr0manthre.innotournament.sidebar;

import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.SidebarInterface;
import eu.pb4.sidebars.api.SidebarUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SuppliedSidebar implements SidebarInterface {
	private final Set<ServerGamePacketListenerImpl> players = new HashSet<>();
	@Getter
	private Sidebar.Priority priority;
	@Getter
	private int updateRate = 1;
	@Getter
    private boolean isActive = false;
	@Setter
	@Getter
	private boolean isDirty = false;
	@Getter
	private Supplier<SidebarData> supplier;

	public void setSupplier(Supplier<SidebarData> supplier) {
		this.supplier = supplier;
		show();
	}

	public void setPriority(Sidebar.Priority priority) {
		this.priority = priority;
		if (this.isActive) {
			for (ServerGamePacketListenerImpl player : this.players) {
				SidebarUtils.updatePriorities(player, this);
			}
		}
	}

	public void setUpdateRate(int updateRate) {
		this.updateRate = Math.max(updateRate, 1);
	}

	public void show() {
		if (!isActive) {
			this.isActive = true;
			for (ServerGamePacketListenerImpl player : this.players) {
				SidebarUtils.addSidebar(player, this);
			}
		}
	}

	public void hide() {
		if (this.isActive) {
			this.isActive = false;
			for (ServerGamePacketListenerImpl player : this.players) {
				SidebarUtils.removeSidebar(player, this);
			}
		}
	}

    public void addPlayer(ServerGamePacketListenerImpl handler) {
		if (this.players.add(handler)) {
			if (isActive) {
				SidebarUtils.addSidebar(handler, this);
			}
		}
	}

	public void removePlayer(ServerGamePacketListenerImpl handler) {
		if (this.players.remove(handler)) {
			if (isActive) {
				if (!handler.player.hasDisconnected()) {
					SidebarUtils.removeSidebar(handler, this);
				}
			}
		}
	}

	public void addPlayer(ServerPlayer player) {
		this.addPlayer(player.connection);
	}

	public void removePlayer(ServerPlayer player) {
		this.removePlayer(player.connection);
	}

	public Set<ServerGamePacketListenerImpl> getPlayerHandlerSet() {
		return Collections.unmodifiableSet(this.players);
	}

	@Override
	public void disconnected(ServerGamePacketListenerImpl handler) {
		this.removePlayer(handler);
	}

	@Override
	public SidebarData getDataFor(ServerGamePacketListenerImpl handler) {
		return supplier == null ? new SidebarData(Component.empty(), List.of()) : supplier.get();
	}
}
