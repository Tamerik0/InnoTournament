package dev.necr0manthre.innotournament.tournament_events.event_data;

import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import lombok.Data;
import net.minecraft.advancements.AdvancementHolder;

@Data
public class TeamAdvancement implements ITeamProvider{
	private final TournamentTeamManager.TournamentTeam team;
	private final AdvancementHolder advancement;
}
