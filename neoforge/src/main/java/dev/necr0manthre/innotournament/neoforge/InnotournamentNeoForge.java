package dev.necr0manthre.innotournament.neoforge;

import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.neoforge.client.InnotournamentNeoforgeClient;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@Mod(Innotournament.MOD_ID)
public final class InnotournamentNeoForge extends Innotournament {
	public InnotournamentNeoForge() {
		onInitialize();
		if(FMLEnvironment.dist.isClient()) {
			new InnotournamentNeoforgeClient().onInitializeClient();
		}
	}
}
