package eu.pb4.sidebars.impl;


import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.List;

public class SidebarAPIMod {
	public static final List<String> FAKE_PLAYER_NAMES = new ArrayList<>();
	public static final Scoreboard SCOREBOARD = new Scoreboard();
	public static final String OBJECTIVE_NAME = "■SidebarApiObj";
	public static final Objective SCOREBOARD_OBJECTIVE = new Objective(
			SCOREBOARD, OBJECTIVE_NAME, ObjectiveCriteria.DUMMY,
			Component.literal("Something went wrong..."), ObjectiveCriteria.RenderType.INTEGER, false, null);

	static {
		for (int x = 0; x < 15; x++) {
			String fakePlayerName = String.format("$.%s", Integer.toHexString(x));
			FAKE_PLAYER_NAMES.add(fakePlayerName);
		}
	}
}
