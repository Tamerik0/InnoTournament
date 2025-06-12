package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.util.ServerBoundObjManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.ref.WeakReference;
import java.util.*;

public class TournamentPlayerManager implements ServerBoundObjManager.Removable {
	private static final ServerBoundObjManager<TournamentPlayerManager> serverToManagerMap = new ServerBoundObjManager<>();
	private final WeakReference<MinecraftServer> serverRef;
	Map<UUID, TournamentPlayer> players = new HashMap<>();
	Map<String, TournamentPlayer> playersByNames = new HashMap<>();

	static {
		serverToManagerMap.register();
	}

	PlayerEvent.PlayerQuit leave = this::leave;
	TickEvent.Player tick = this::tick;

	private TournamentPlayerManager(MinecraftServer server) {
		this.serverRef = new WeakReference<>(server);
		PlayerEvent.PLAYER_QUIT.register(leave);
		TickEvent.PLAYER_POST.register(tick);
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

	public TournamentPlayer get(String name) {
		var player = getServer().getPlayerList().getPlayerByName(name);
		if (player == null)
			return playersByNames.computeIfAbsent(name, n -> players.computeIfAbsent(getServer().getProfileCache().get(name).get().getId(), h -> new TournamentPlayer(name, getServer())));
		return get(player);
	}

	public static TournamentPlayer getStatic(ServerPlayer player) {
		return TournamentPlayerManager.get(player.server).get(player);
	}

	public List<TournamentPlayer> getPlayers() {
		return getServer().getPlayerList().getPlayers().stream().map(this::get).toList();
	}


	private void leave(ServerPlayer player) {
//		get(player).player = null;
	}

	private void tick(Player xyecoc) {
		if (xyecoc instanceof ServerPlayer player) {
			get(player).player = player;
			playersByNames.put(xyecoc.getScoreboardName(), get(player));
			get(player).tick();
		}
	}

	@Override
	public void onRemove() {
		PlayerEvent.PLAYER_QUIT.unregister(leave);
		TickEvent.PLAYER_POST.unregister(tick);
	}
}
