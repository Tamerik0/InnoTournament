package dev.necr0manthre.innotournament.tournament;

import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;

public class TournamentPlayer {
	public final ServerPlayer player;
	public int lives = 3;
	public WeakReference<Tournament> tournament = null;

	public TournamentPlayer(ServerPlayer player) {
		this.player = player;
	}
}
