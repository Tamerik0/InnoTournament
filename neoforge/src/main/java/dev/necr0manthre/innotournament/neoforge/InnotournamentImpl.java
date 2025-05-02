package dev.necr0manthre.innotournament.neoforge;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import dev.necr0manthre.innotournament.init.InnoBlocks;
import dev.necr0manthre.innotournament.init.InnoItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.EventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InnotournamentImpl{

	public static void registerContent(ResourceKey<? extends Registry<?>> registry, Runnable registerer) {
		ModList.get().getModContainerById(Innotournament.MOD_ID).get().getEventBus().addListener((RegisterEvent event) -> {
			if(event.getRegistryKey().equals(registry))
				registerer.run();
		});
	}
}
