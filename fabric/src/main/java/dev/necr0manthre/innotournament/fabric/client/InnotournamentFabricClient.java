package dev.necr0manthre.innotournament.fabric.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.necr0manthre.innotournament.client.InnotournamentClient;
import dev.necr0manthre.innotournament.client.blockentityrenderers.ItemGeneratorRenderer;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import net.fabricmc.api.ClientModInitializer;

public final class InnotournamentFabricClient extends InnotournamentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		super.onInitializeClient();
		BlockEntityRendererRegistry.register(InnoBlockEntities.ITEM_GENERATOR_BLOCK_ENTITY_TYPE, ItemGeneratorRenderer::new);
	}
}
