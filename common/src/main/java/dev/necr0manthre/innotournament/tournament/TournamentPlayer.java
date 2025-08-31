//package dev.necr0manthre.innotournament.tournament;
//
//import net.minecraft.network.chat.ClickEvent;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.HoverEvent;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.scores.PlayerTeam;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class TournamentPlayer {
//	ServerPlayer player;
//
//	public Optional<ServerPlayer> getServerPlayer() {
//		return Optional.ofNullable(player);
//	}
//
//	private String name;
//	public int lives = 3;
//	public WeakReference<Tournament> tournament = null;
//	public WeakReference<MinecraftServer> server;
//	public final List<Invite> invites = new ArrayList<>();
//
//	public TournamentPlayer(ServerPlayer player) {
//		this.player = player;
//		this.name = player.getScoreboardName();
//		this.server = new WeakReference<>(player.server);
//	}
//
//	public TournamentPlayer(String name, MinecraftServer server) {
//		this.name = name;
//		this.server = new WeakReference<>(server);
//	}
//
//	public void tick() {
//		if (player != null) {
//			this.name = player.getScoreboardName();
//		}
//		invites.removeIf(Invite::tick);
//	}
//
//	public void invite(TournamentTeamManager.TournamentTeam team, int inviteTime) {
//		invites.add(new Invite(this, team, inviteTime));
//	}
//
//	public String getName() {
//		if (player != null)
//			return name = player.getScoreboardName();
//		return name;
//	}
//
//	public Component getDisplayName() {
//		return PlayerTeam.formatNameForTeam(server.get().getScoreboard().getPlayerTeam(name), Component.literal(name)).withStyle((style) -> style.withClickEvent(new ClickEvent.SuggestCommand("/tell " + name + " ")).withHoverEvent(new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, server.get().getProfileCache().get(name).get().getId(), Component.literal(name)))).withInsertion(name));
//	}
//}
