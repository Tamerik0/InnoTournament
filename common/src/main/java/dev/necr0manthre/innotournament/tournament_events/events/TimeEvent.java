package dev.necr0manthre.innotournament.tournament_events.events;

import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;


public class TimeEvent extends AbstractTournamentEvent<Object> {
	private final long time;
	private boolean completed;
	private Consumer<Object> callback;
	private Tournament tournament;

	public TimeEvent(long time) {
		this.time = time;
	}

	@Override
	protected void subscribe(Tournament tournament, Consumer<Object> callback) {
		this.tournament = tournament;
		this.callback = callback;
		completed = false;
		TickEvent.SERVER_PRE.register(this::tick);
	}

	@Override
	public void remove() {
		TickEvent.SERVER_PRE.unregister(this::tick);
	}

	private void tick(MinecraftServer server) {
		if (tournament.getElapsedTime() >= time && !completed) {
			completed = true;
			callback.accept(null);
		}
	}
}
