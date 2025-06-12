package dev.necr0manthre.innotournament.util;

import dev.architectury.event.events.client.ClientTickEvent;

public class ClientScheduler {
	private static final Scheduler clientPostScheduler = new Scheduler();
	private static final Scheduler clientPreScheduler = new Scheduler();

	public static void init() {
	}

	static {
		ClientTickEvent.CLIENT_POST.register(event -> clientPostScheduler.tick());
		ClientTickEvent.CLIENT_POST.register(event -> clientPostScheduler.tick());
	}

	public static void scheduleClientPost(int ticks, Runnable task) {
		clientPostScheduler.schedule(ticks, task);
	}

	public static void scheduleClientPre(int ticks, Runnable task) {
		clientPreScheduler.schedule(ticks, task);
	}
}
