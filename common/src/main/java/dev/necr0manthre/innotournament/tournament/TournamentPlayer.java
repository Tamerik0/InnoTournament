package dev.necr0manthre.innotournament.tournament;

import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TournamentPlayer {
	public ServerPlayer player;
	public int lives = 3;
	public WeakReference<Tournament> tournament = null;
	public final List<Invite> invites = new ArrayList<>();

	public TournamentPlayer(ServerPlayer player) {
		this.player = player;
	}

	public void tick() {
		invites.removeIf(Invite::tick);
	}

	public void invite(TournamentTeam team, int inviteTime) {
		invites.add(new Invite(this, team, inviteTime));
	}
}
