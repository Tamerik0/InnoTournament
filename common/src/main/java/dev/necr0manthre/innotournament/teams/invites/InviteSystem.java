package dev.necr0manthre.innotournament.teams.invites;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.TickEvent;
import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.function.Consumer;

public class InviteSystem {
    public static final Event<Consumer<Invite>> INVITE_EVENT = EventFactory.createLoop();
    public static final Event<Consumer<Invite>> INVITE_EXPIRATION_EVENT = EventFactory.createLoop();

    public static void register() {
        TickEvent.SERVER_POST.register(InviteSystem::tick);
    }

    public static void tick(MinecraftServer server) {
        var teamManager = TeamManager.get(server);
        teamManager.getAllTeams().forEach(team -> {
            var invites = Invites.getComponent(team);
            synchronized (invites.invites) {
                invites.invites.removeIf(invite -> {
                    invite.tick();
                    if (invite.isExpired()) {
                        INVITE_EXPIRATION_EVENT.invoker().accept(invite);
                        Invites.getComponent(invite.player).invites.remove(invite);
                        return true;
                    }
                    return false;
                });
            }
        });
    }

    public static boolean isPlayerInvitedToTeam(Entity player, Entity team) {
        var invites = Invites.getComponent(team);
        synchronized (invites.invites) {
            return invites.invites.stream().anyMatch(invite -> invite.player.equals(player) && !invite.isExpired());
        }
    }

    public static void invitePlayerToTeam(Entity player, Entity team, int lifetime) {
        if (isPlayerInvitedToTeam(player, team)) return;
        var invite = new Invite(team, player, lifetime);
        Invites.getComponent(team).invites.add(invite);
        Invites.getComponent(player).invites.add(invite);
        INVITE_EVENT.invoker().accept(invite);
    }

    public static void rejectInvite(Entity player, Entity team) {
        if (!isPlayerInvitedToTeam(player, team)) return;
        var invites = Invites.getComponent(team);
        synchronized (invites.invites) {
            invites.invites.removeIf(invite -> {
                        if (invite.player.equals(player)) {
                            Invites.getComponent(player).invites.remove(invite);
                            return true;
                        }
                        return false;
                    }
            );
        }
    }

    public static Set<Invite> getInvitesForPlayer(Entity player) {
        return Set.copyOf(Invites.getComponent(player).invites);
    }

    public static Set<Invite> getInvitesForTeam(Entity team) {
        return Set.copyOf(Invites.getComponent(team).invites);
    }
}
