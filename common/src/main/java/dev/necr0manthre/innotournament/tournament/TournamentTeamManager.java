package dev.necr0manthre.innotournament.tournament;

import lombok.Getter;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.lang.ref.WeakReference;
import java.util.*;

public class TournamentTeamManager {
	private static final WeakHashMap<MinecraftServer, TournamentTeamManager> serverToManagerMap = new WeakHashMap<>();
	private final WeakReference<MinecraftServer> serverRef;
	WeakHashMap<PlayerTeam, TournamentTeam> teams = new WeakHashMap<>();

	private TournamentTeamManager(MinecraftServer server) {
		this.serverRef = new WeakReference<>(server);
	}

	public static TournamentTeamManager get(MinecraftServer server) {
		return serverToManagerMap.computeIfAbsent(server, TournamentTeamManager::new);
	}

	public MinecraftServer getServer() {
		return serverRef.get();
	}

	public TournamentTeam get(PlayerTeam team) {
		if (team == null)
			return null;
		return teams.computeIfAbsent(team, t -> new TournamentTeam(t, TournamentPlayerManager.get(getServer())));
	}

	public List<TournamentTeam> getTeams() {
		return getServer().getScoreboard().getPlayerTeams().stream().map(this::get).toList();
	}

	public TournamentTeam getTeam(TournamentPlayer player) {
		return get(player.player.getTeam());
	}

	public String createNewUnnamedTeamName() {
		for (int i = 0; ; i++) {
			var name = "team" + i;
			if (getServer().getScoreboard().getPlayerTeam(name) == null)
				return name;
		}
	}

	public static void broadcastToTeam(Component text, TournamentTeam team, TournamentPlayerManager playerManager) {
		for (var player : team.getPlayers())
			player.player.sendSystemMessage(text);
	}

	public class TournamentTeam {
		@Getter
		private PlayerTeam playerTeam;
		public final List<TournamentPlayer> requests = new ArrayList<>();
		private final Set<AdvancementHolder> advancements = new HashSet<>();
		private final TournamentPlayerManager playerManager;
		public double score = 0;
		public boolean acceptAll = false;
		public TournamentPlayer owner = null;
		public WeakReference<Tournament> tournament;

		public void makeAdvancement(AdvancementHolder advancement) {
			this.advancements.add(advancement);
			for (var player : getPlayers()) {
				var advancementProgress = player.player.getAdvancements().getOrStartProgress(advancement);
				for (String string : advancementProgress.getRemainingCriteria()) {
					player.player.getAdvancements().award(advancement, string);
				}
			}
		}

		public void clearAdvancements() {
			advancements.clear();
		}

		public boolean hasAdvancement(AdvancementHolder advancement) {
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
			if (scoreboard.getPlayerTeam(newName) != null)
				return;
			var players = playerTeam.getPlayers().stream().toList();
			var displayName = playerTeam.getDisplayName();
			var color = playerTeam.getColor();
			scoreboard.removePlayerTeam(playerTeam);
			playerTeam = scoreboard.addPlayerTeam(newName);
			playerTeam.setDisplayName(displayName);
			playerTeam.setColor(color);
			for (var player : players) {
				scoreboard.addPlayerToTeam(player, playerTeam);
			}
			teams.put(playerTeam, this);
		}

	}
}
