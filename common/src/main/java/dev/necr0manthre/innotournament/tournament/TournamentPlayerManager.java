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
	static {
		serverToManagerMap.register();
	}

	private TournamentPlayerManager(MinecraftServer server) {
		this.serverRef = new WeakReference<>(server);
		PlayerEvent.PLAYER_QUIT.register(this::leave);
		TickEvent.PLAYER_POST.register(this::tick);
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

	public static TournamentPlayer getStatic(ServerPlayer player) {
		return TournamentPlayerManager.get(player.server).get(player);
	}

	public List<TournamentPlayer> getPlayers() {
		return getServer().getPlayerList().getPlayers().stream().map(this::get).toList();
	}


	private void leave(ServerPlayer player) {
		get(player).player = null;
	}

	private void tick(Player xyecoc) {
		if (xyecoc instanceof ServerPlayer player) {
			get(player).player = player;
			get(player).tick();
		}
	}

	@Override
	public void onRemove() {
		PlayerEvent.PLAYER_QUIT.unregister(this::leave);
		TickEvent.PLAYER_POST.unregister(this::tick);
	}
}
