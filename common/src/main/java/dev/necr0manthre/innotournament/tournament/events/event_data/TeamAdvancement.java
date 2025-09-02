package dev.necr0manthre.innotournament.tournament.events.event_data;

import dev.dominion.ecs.api.Entity;
import lombok.Data;
import net.minecraft.advancements.AdvancementHolder;

@Data
public class TeamAdvancement implements ITeamProvider{
	private final Entity team;
	private final AdvancementHolder advancement;
}
