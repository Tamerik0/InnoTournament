package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.PlayerEvent;
import dev.necr0manthre.innotournament.players.PlayerManager;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import dev.necr0manthre.innotournament.tournament.components.TeamAdvancements;
import dev.necr0manthre.innotournament.tournament.events.ITeamAdvancementEventHandler;
import dev.necr0manthre.innotournament.tournament.events.event_data.TeamAdvancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;

public class TournamentAdvancementHandler {
    private final WeakReference<Tournament> tournament;
    public final Event<ITeamAdvancementEventHandler> teamAdvancementEvent = EventFactory.createLoop();

    public TournamentAdvancementHandler(Tournament tournament) {
        this.tournament = new WeakReference<>(tournament);
    }

    public void register() {
        PlayerEvent.PLAYER_ADVANCEMENT.register(handle);
        PlayerEvent.PLAYER_JOIN.register(handleJoin);
    }

    PlayerEvent.PlayerAdvancement handle = this::handle;
    PlayerEvent.PlayerJoin handleJoin = this::handleJoin;

    private void handle(ServerPlayer serverPlayer, AdvancementHolder advancement) {
        var player = PlayerManager.getEntity(serverPlayer);
        var team = TeamManager.getPlayersTeam(player);
        if (team.isEmpty())
            return;
        if (!TeamAdvancements.get(team.get()).advancements.contains(advancement)) {
            TeamAdvancements.get(team.get()).advancements.add(advancement);
            teamAdvancementEvent.invoker().handle(new TeamAdvancement(team.get(), advancement));
        }
        for (var player1 : TeamManager.getPlayers(team.get())) {
            PlayerManager.getServerPlayer(player1).ifPresent(p -> {
                PlayerAdvancements advancements = p.getAdvancements();
                var progress = advancements.getOrStartProgress(advancement);
                for (var c : progress.getRemainingCriteria())
                    advancements.award(advancement, c);
            });
        }
    }

    private void handleJoin(ServerPlayer player) {
        var tournament = this.tournament.get();
        if (tournament == null)
            return;
        var playerEntity = PlayerManager.getEntity(player);
        var team = TeamManager.getPlayersTeam(playerEntity);
        if (team.isEmpty())
            return;
        for (var advancement : TeamAdvancements.get(team.get()).advancements) {
            PlayerAdvancements advancements = player.getAdvancements();
            var progress = advancements.getOrStartProgress(advancement);
            for (var c : progress.getRemainingCriteria())
                advancements.award(advancement, c);
        }
    }

    public void unregister() {
        PlayerEvent.PLAYER_ADVANCEMENT.unregister(handle);
        PlayerEvent.PLAYER_JOIN.unregister(handleJoin);
    }
}
