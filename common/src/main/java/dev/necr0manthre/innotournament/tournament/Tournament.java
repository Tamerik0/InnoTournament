package dev.necr0manthre.innotournament.tournament;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.necr0manthre.innotournament.sidebar.SidebarManager;
import dev.necr0manthre.innotournament.tournament_events.AbstractTournamentEvent;
import dev.necr0manthre.innotournament.tournament_events.ITournamentEventListener;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.SidebarInterface;
import eu.pb4.sidebars.api.lines.ImmutableSidebarLine;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class Tournament {
	private static final WeakHashMap<MinecraftServer, Tournament> serverToManagerMap = new WeakHashMap<>();

	public final Event<Consumer<Object>> onPrepare = EventFactory.createLoop();
	public final Event<Consumer<Object>> onStart = EventFactory.createLoop();
	public final Event<Consumer<Object>> onEnd = EventFactory.createLoop();
	public final Event<Consumer<Object>> onRemove = EventFactory.createLoop();
	private final GameType preStartGameMode;
	private final Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> preStartEvents;
	private final Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> mainEvents;
	BlockPos tournamentSpawn;
	ResourceKey<Level> tournamentSpawnDimension;
	BlockPos tournamentPreSpawn;
	ResourceKey<Level> tournamentPreSpawnDimension;
	ResourceLocation startBoxStructureResourceLocation;
	BlockPos startBoxPos;
	long startTime;
	@Getter
	int phase = 0;
	private StructureTemplate startBoxStructure;
	private WeakReference<MinecraftServer> serverRef = new WeakReference<>(null);
	private SidebarManager sidebarManager;

	public Tournament(BlockPos tournamentSpawn,
	                  ResourceKey<Level> tournamentSpawnDimension,
	                  BlockPos tournamentPreSpawn,
	                  ResourceKey<Level> tournamentPreSpawnDimension,
	                  ResourceLocation startBoxStructureResourceLocation,
	                  BlockPos startBoxPos,
	                  GameType preStartGameMode,
	                  Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> preStartEvents,
	                  Map<AbstractTournamentEvent<?>, Set<ITournamentEventListener<?>>> mainEvents) {
		this.tournamentSpawn = tournamentSpawn;
		this.tournamentSpawnDimension = tournamentSpawnDimension;
		this.tournamentPreSpawn = tournamentPreSpawn;
		this.tournamentPreSpawnDimension = tournamentPreSpawnDimension;
		this.startBoxStructureResourceLocation = startBoxStructureResourceLocation;
		this.startBoxPos = startBoxPos;
		this.preStartGameMode = preStartGameMode;
		this.preStartEvents = preStartEvents;
		this.mainEvents = mainEvents;
	}

	public static void setTournament(MinecraftServer server, Tournament tournament) {
		if (get(server) != null)
			try {
				get(server).remove();
			} catch (Exception ignore) {
			}
		serverToManagerMap.put(server, tournament);
	}

	public static Tournament get(MinecraftServer server) {
		return serverToManagerMap.getOrDefault(server, null);
	}

	public MinecraftServer getServer() {
		return serverRef.get();
	}

	public void updateSidebars() {
		for (var player : getServer().getPlayerList().getPlayers())
			sidebarManager.updateSidebar(player);
	}

	public void remove() {
		onRemove.invoker().accept(null);
		serverToManagerMap.remove(getServer());
		for (var event : preStartEvents.keySet()) {
			event.remove();
		}
		for (var event : mainEvents.keySet()) {
			event.remove();
		}
		sidebarManager.remove();
		PlayerEvent.PLAYER_RESPAWN.unregister(this::handlePreStartRespawn);
		PlayerEvent.PLAYER_RESPAWN.unregister(this::handleRespawn);
		TickEvent.SERVER_POST.unregister(this::tick);
	}

	public void placeStartBox() {
		startBoxStructure = getServer().getStructureManager().getOrCreate(startBoxStructureResourceLocation);
		startBoxStructure.placeInWorld(getServer().getLevel(tournamentPreSpawnDimension), startBoxPos, startBoxPos, new StructurePlaceSettings(), new LegacyRandomSource(0), 2 | 816);
	}

	public void removeStartBox() {
		var level = getServer().getLevel(tournamentPreSpawnDimension);
		for (var pos : BlockPos.betweenClosed(startBoxPos, startBoxPos.offset(startBoxStructure.getSize()))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
		}
	}

	private void handlePreStartRespawn(ServerPlayer player, boolean huy, Entity.RemovalReason removalReason) {
		player.setRespawnPosition(new ServerPlayer.RespawnConfig(tournamentPreSpawnDimension, tournamentPreSpawn, 0, true), false);
		player.teleport(new TeleportTransition(getServer().getLevel(tournamentPreSpawnDimension), tournamentPreSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
		}));
	}

	private void handleRespawn(ServerPlayer player, boolean huy, Entity.RemovalReason removalReason) {
		player.setRespawnPosition(new ServerPlayer.RespawnConfig(tournamentSpawnDimension, tournamentSpawn, 0, true), false);
		player.teleport(new TeleportTransition(getServer().getLevel(tournamentSpawnDimension), tournamentSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
		}));
	}

	public void checkPlayer(ServerPlayer player) {
		if (phase == 1) {
			if (player.gameMode.getGameModeForPlayer() != preStartGameMode)
				player.setGameMode(preStartGameMode);
		} else if (phase == 2) {
			var tournamentPlayer = TournamentPlayerManager.get(getServer()).get(player);
			if (tournamentPlayer.tournament == null || tournamentPlayer.tournament.get() != this) {
				tournamentPlayer.tournament = new WeakReference<>(this);
				tournamentPlayer.lives = 3;
			}
		}
	}

	public void checkAllPlayers() {
		for (var player : getServer().getPlayerList().getPlayers()) {
			checkPlayer(player);
		}
	}

	public void prepare(MinecraftServer server) {
		if (phase == 1)
			throw new IllegalStateException("Tournament is already prepared");
		if (phase == 2)
			throw new IllegalStateException("Tournament is already started");
		if (phase == 3)
			throw new IllegalStateException("Tournament is already ended");
		if (phase > 3)
			throw new IllegalStateException("WTF with tournament phase");
		serverRef = new WeakReference<>(server);
		this.sidebarManager = new SidebarManager(server, player ->
				                                                 new SidebarManager.SuppliedSidebarData(Sidebar.Priority.OVERRIDE, 400, () -> {
					                                                 var title = Component.literal("Innotournament v2!!!");
					                                                 var team = TournamentTeamManager.get(getServer()).get(player.getTeam());
					                                                 var teamName = team == null ? "No team" : team.playerTeam.getName();
					                                                 List<ImmutableSidebarLine> lines = new ArrayList<>();
					                                                 int i = 0;
					                                                 lines.add(new ImmutableSidebarLine(i--, Component.literal(teamName), BlankFormat.INSTANCE));
					                                                 if (team != null) {
						                                                 lines.add(new ImmutableSidebarLine(i--, Component.literal("Score: " + team.score), BlankFormat.INSTANCE));
						                                                 for (var tournamentPlayer : team.getPlayers(TournamentPlayerManager.get(getServer()))) {
							                                                 lines.add(new ImmutableSidebarLine(i--, Component.literal(tournamentPlayer.player.getScoreboardName()).append("  [%d]".formatted(tournamentPlayer.lives)), BlankFormat.INSTANCE));
						                                                 }
					                                                 }
					                                                 return new SidebarInterface.SidebarData(title, lines);
				                                                 }));
		server.getLevel(tournamentPreSpawnDimension).setDefaultSpawnPos(tournamentPreSpawn, 0);
		PlayerEvent.PLAYER_RESPAWN.register(this::handlePreStartRespawn);
		PlayerEvent.PLAYER_JOIN.register(this::checkPlayer);
		phase = 1;
		for (var event : preStartEvents.keySet()) {
			event.subscribe(this);
			for (var listener : preStartEvents.get(event))
				event.addListener((ITournamentEventListener) listener);
		}
		checkAllPlayers();
		onPrepare.invoker().accept(null);

	}

	public void start() {
		if (phase == 0)
			throw new IllegalStateException("Tournament is not prepared yet");
		if (phase == 2)
			throw new IllegalStateException("Tournament is already started");
		if (phase == 3)
			throw new IllegalStateException("Tournament is already ended");
		if (phase > 3)
			throw new IllegalStateException("WTF with tournament phase");
		for (var event : mainEvents.keySet()) {
			event.subscribe(this);
			for (var listener : mainEvents.get(event))
				event.addListener((ITournamentEventListener) listener);
		}
		for (var event : preStartEvents.keySet()) {
			event.remove();
		}
		phase = 2;
		PlayerEvent.PLAYER_RESPAWN.unregister(this::handlePreStartRespawn);
		PlayerEvent.PLAYER_RESPAWN.register(this::handleRespawn);
		TickEvent.SERVER_POST.register(this::tick);
		startTime = getServer().overworld().getGameTime();
		getServer().overworld().getWorldBorder().setCenter(tournamentSpawn.getX(), tournamentSpawn.getZ());
		getServer().getLevel(tournamentSpawnDimension).setDefaultSpawnPos(tournamentSpawn, 0);
		getServer().setPvpAllowed(true);
		for (var player : getServer().getPlayerList().getPlayers()) {
			player.teleport(new TeleportTransition(getServer().getLevel(tournamentSpawnDimension), tournamentSpawn.getCenter(), Vec3.ZERO, 0, 0, entity -> {
			}));
			player.setGameMode(GameType.ADVENTURE);
		}
		for (var team : TournamentTeamManager.get(getServer()).getTeams()) {
			team.score = 0;
		}
		onStart.invoker().accept(null);
		checkAllPlayers();
	}

	private void tick(MinecraftServer server) {

	}

	public long getElapsedTime() {
		if (phase >= 2)
			return getServer().overworld().getGameTime() - startTime;
		return 0;
	}
}
