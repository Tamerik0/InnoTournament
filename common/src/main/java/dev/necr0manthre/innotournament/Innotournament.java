package dev.necr0manthre.innotournament;

import com.google.common.eventbus.Subscribe;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.necr0manthre.innotournament.commands.TeamsCommand;
import dev.necr0manthre.innotournament.commands.TournamentCommand;
import dev.necr0manthre.innotournament.events.EndServerReloadEvent;
import dev.necr0manthre.innotournament.init.*;
import dev.necr0manthre.innotournament.util.ClientScheduler;
import dev.necr0manthre.innotournament.util.ServerScheduler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Innotournament {
	public static final String MOD_ID = "innotournament";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static float getGeneratorSpeed(MinecraftServer server) {
		return 1;
	}

	@ExpectPlatform
	public static void registerContent(ResourceKey<? extends Registry<?>> registry, Runnable registerer) {
	}

	void init(MinecraftServer server) {
		InnoBlocks.ITEM_GENERATOR_BLOCK.Init(server.registryAccess());
		InnoBlocks.BOOK_GENERATOR_BLOCK.Init(server.registryAccess());
		InnoBlocks.NETHER_GENERATOR_BLOCK.Init(server.registryAccess());
	}

	LifecycleEvent.ServerState init = this::init;

	public void onInitialize() {
		InnoTournamentEvents.init();
		InnoTournamentEventHandlers.init();
		registerContent(Registries.BLOCK, InnoBlocks::initialize);
		registerContent(Registries.ITEM, InnoItems::initialize);
		registerContent(Registries.BLOCK_ENTITY_TYPE, InnoBlockEntities::initialize);
		EndServerReloadEvent.EVENT.register(init);
		LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
			if (level == level.getServer().overworld())
				init(level.getServer());
		});
		CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
			TeamsCommand.register(dispatcher, registryAccess, environment);
			TournamentCommand.register(dispatcher, registryAccess, environment);
		});
		ServerScheduler.init();
	}
}
