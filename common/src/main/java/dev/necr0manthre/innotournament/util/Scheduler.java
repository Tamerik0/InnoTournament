package dev.necr0manthre.innotournament.util;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
	private final Object lock = new Object();
	private final List<Task> pendingQueue = new ArrayList<>();
	private final List<Task> activeQueue = new ArrayList<>();

	public void schedule(int ticks, Runnable task) {
		synchronized (lock) {
			pendingQueue.add(new Task(ticks, task));
		}
	}

	public void tick() {
		synchronized (lock) {
			if (!pendingQueue.isEmpty()) {
				activeQueue.addAll(pendingQueue);
				pendingQueue.clear();
			}
		}

		activeQueue.removeIf(Task::tick);
	}

	@AllArgsConstructor
	private static class Task {
		int ticks;
		Runnable action;

		boolean tick() {
			if (--ticks > 0)
				return false;
			action.run();
			return true;
		}
	}
}
