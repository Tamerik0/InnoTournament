package dev.necr0manthre.teams;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;

public class TeamsEventHandler {
    static void init() {
    }

    static {
        TickEvent.SERVER_POST.register(TeamsEventHandler::serverTick);
    }

    private static void serverTick(MinecraftServer server) {
        var teamManager = TeamManager.get(server);
        teamManager.getAllTeams().forEach(team -> {
            if (!team.isPersistent() && !team.isValid())
                teamManager.ecs.deleteEntity(team.ecsEntity);
        });
    }
}

