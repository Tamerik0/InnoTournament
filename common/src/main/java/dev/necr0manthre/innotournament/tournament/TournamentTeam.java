package dev.necr0manthre.innotournament.tournament;

import lombok.Getter;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.*;

public class TournamentTeam {
	@Getter
	private PlayerTeam playerTeam;
	public final List<TournamentPlayer> requests = new ArrayList<>();
	private final Set<AdvancementHolder> advancements = new HashSet<>();
	private final TournamentPlayerManager playerManager;
	public double score = 0;
	public boolean acceptAll = false;
	public TournamentPlayer owner = null;

	public void makeAdvancement(AdvancementHolder advancement) {
		this.advancements.add(advancement);
		for (var player : getPlayers()) {
			var advancementProgress = player.player.getAdvancements().getOrStartProgress(advancement);
			for (String string : advancementProgress.getRemainingCriteria()) {
				player.player.getAdvancements().award(advancement, string);
			}
		}
	}

	public boolean hasAdvancement(Advancement advancement) {
		return advancements.contains(advancement);
	}

	public TournamentTeam(PlayerTeam playerTeam, TournamentPlayerManager playerManager) {
		this.playerTeam = playerTeam;
		this.playerManager = playerManager;
	}

	public TournamentTeam(PlayerTeam playerTeam, TournamentPlayerManager playerManager, TournamentPlayer owner) {
		this(playerTeam, playerManager);
		this.owner = owner;
	}

	public boolean removePlayer(TournamentPlayer player) {
		if (!playerTeam.getPlayers().contains(player.player.getScoreboardName()))
			return false;
		playerTeam.getScoreboard().removePlayerFromTeam(player.player.getScoreboardName(), playerTeam);
		if (owner == player) {
			if (playerTeam.getPlayers().isEmpty()) {
				owner = null;
				acceptAll = true;
			}
			owner = getPlayers().getFirst();
		}
		return true;
	}

	public boolean addPlayer(TournamentPlayer player) {
		if (playerTeam.getPlayers().contains(player.player.getScoreboardName()))
			return false;
		playerTeam.getScoreboard().addPlayerToTeam(player.player.getScoreboardName(), playerTeam);
		if (getPlayers().isEmpty())
			owner = player;
		return true;
	}


//	public void requestJoin(TournamentPlayer player) {
//		if (requests.contains(player)) return;
//		requests.add(player);
//	}

	public List<TournamentPlayer> getPlayers() {
		return playerTeam.getPlayers().stream().map(playerManager::get).toList();
	}

	public void changeName(String newName) {
		var scoreboard = playerTeam.getScoreboard();
		var players = playerTeam.getPlayers();
		var displayName = playerTeam.getDisplayName();
		var color = playerTeam.getColor();
		playerTeam = scoreboard.addPlayerTeam(newName);
		playerTeam.setDisplayName(displayName);
		playerTeam.setColor(color);
		for (var player : players) {
			scoreboard.addPlayerToTeam(player, playerTeam);
		}
	}

}
