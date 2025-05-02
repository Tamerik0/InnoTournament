package dev.necr0manthre.innotournament.fabric;

import dev.necr0manthre.innotournament.Innotournament;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;

import java.util.function.Consumer;

public class InnotournamentImpl{
	public static void registerContent(ResourceKey<? extends Registry<?>> registry,Runnable registerer) {
		registerer.run();

	}
}
