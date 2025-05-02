package dev.necr0manthre.innotournament.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.LifecycleEvent;

public interface EndServerReloadEvent {
	Event<LifecycleEvent.ServerState> EVENT = EventFactory.createLoop();
}
