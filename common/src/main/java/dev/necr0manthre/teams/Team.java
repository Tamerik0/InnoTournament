package dev.necr0manthre.teams;

import dev.dominion.ecs.api.Entity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.scores.PlayerTeam;

import java.lang.ref.WeakReference;

public class Team {
    public final Entity ecsEntity;
    PlayerTeam playerTeam;
    private final WeakReference<TeamManager> teamManager;
    /**
     * makes team not to remove when invalid
     */
    @Getter
    @Setter
    private boolean persistent = false;

    public TeamManager getTeamManager() {
        return teamManager.get();
    }

    Team(TeamManager teamManager) {
        this.teamManager = new WeakReference<>(teamManager);
        ecsEntity = getTeamManager().ecs.createEntity();
    }

    public PlayerTeam getPlayerTeam() {
        if (playerTeam != null && playerTeam.getScoreboard().getPlayerTeam(playerTeam.getName()) != playerTeam)
            return null;
        return playerTeam;
    }

    public void attachToPlayerTeam(PlayerTeam playerTeam) {
        getTeamManager().attachTeamEntity(this, playerTeam);
    }

    public boolean isValid() {
        return getPlayerTeam() != null;
    }
}
