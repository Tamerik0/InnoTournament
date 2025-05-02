package dev.necr0manthre.innotournament.tournament;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;
import java.util.*;

public class TournamentPlayerManager {
	private static final WeakHashMap<MinecraftServer, TournamentPlayerManager> serverToManagerMap = new WeakHashMap<>();
	private final WeakReference<MinecraftServer> serverRef;
	Map<UUID, TournamentPlayer> players = new HashMap<>();

	private TournamentPlayerManager(MinecraftServer server) {
		this.serverRef = new WeakReference<>(server);
	}

	public static TournamentPlayerManager get(MinecraftServer server) {
		return serverToManagerMap.computeIfAbsent(server, TournamentPlayerManager::new);
	}

	public MinecraftServer getServer() {
		return serverRef.get();
	}

	public TournamentPlayer get(ServerPlayer player) {
		return players.computeIfAbsent(player.getUUID(), id -> new TournamentPlayer(player));
	}

	public List<TournamentPlayer> getPlayers() {
		return getServer().getPlayerList().getPlayers().stream().map(this::get).toList();
	}
}
