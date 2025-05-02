package dev.necr0manthre.innotournament.tournament_events.handlers;

import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament_events.ITournamentEventListener;

public class WorldBorderAction implements ITournamentEventListener<Object> {
	private final long shrinkTime;
	private final double size;

	public WorldBorderAction(double size, long shrinkTime) {
		this.shrinkTime = shrinkTime;
		this.size = size;
	}

	@Override
	public void listen(Tournament tournament, Object value) {
		if(shrinkTime != 0)
			tournament.getServer().overworld().getWorldBorder().lerpSizeBetween(tournament.getServer().overworld().getWorldBorder().getSize(), size, shrinkTime);
		else
			tournament.getServer().overworld().getWorldBorder().setSize(size);
	}
}
