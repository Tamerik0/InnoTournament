package eu.pb4.sidebars.api;

import eu.pb4.sidebars.api.lines.ImmutableSidebarLine;
import eu.pb4.sidebars.api.lines.SidebarLine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;


@SuppressWarnings({"unused"})
public interface SidebarInterface {

	Sidebar.Priority getPriority();

	default int getUpdateRate() {
		return 1;
	}

	boolean isActive();

	void disconnected(ServerGamePacketListenerImpl handler);

	default boolean manualTextUpdates() {
		return false;
	}

	SidebarData getDataFor(ServerGamePacketListenerImpl handler);

	record SidebarData(Component title, List<ImmutableSidebarLine> lines) {
	}
}
