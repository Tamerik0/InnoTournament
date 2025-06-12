package dev.necr0manthre.innotournament.client;


import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.necr0manthre.innotournament.client.blockentityrenderers.ItemGeneratorRenderer;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import dev.necr0manthre.innotournament.util.ClientScheduler;
import dev.necr0manthre.innotournament.util.ServerScheduler;

public class InnotournamentClient {
	public void onInitializeClient() {
//		BlockRenderLayerMap.INSTANCE.putBlock(InnoBlocks.ITEM_GENERATOR_BLOCK, RenderType.cutout());
		ClientScheduler.init();
	}
}
