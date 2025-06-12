package dev.necr0manthre.innotournament.util;

import dev.architectury.event.events.common.TickEvent;

public class ServerScheduler {
	private static final Scheduler serverPostScheduler = new Scheduler();
	private static final Scheduler serverPreScheduler = new Scheduler();

	public static void init() {
	}

	static {
		TickEvent.SERVER_POST.register(event -> serverPostScheduler.tick());
		TickEvent.SERVER_PRE.register(event -> serverPreScheduler.tick());
	}

	public static void scheduleServerPost(int ticks, Runnable task) {
		serverPostScheduler.schedule(ticks, task);
	}

	public static void scheduleServerPre(int ticks, Runnable task) {
		serverPreScheduler.schedule(ticks, task);
	}
}
