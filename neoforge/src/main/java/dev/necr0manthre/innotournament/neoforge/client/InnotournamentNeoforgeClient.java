package dev.necr0manthre.innotournament.neoforge.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.client.InnotournamentClient;
import dev.necr0manthre.innotournament.client.blockentityrenderers.ItemGeneratorRenderer;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Innotournament.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class InnotournamentNeoforgeClient extends InnotournamentClient {
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerEvent(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
			BlockEntityRendererRegistry.register(InnoBlockEntities.ITEM_GENERATOR_BLOCK_ENTITY_TYPE, ItemGeneratorRenderer::new);
		}
	}

	@Override
	public void onInitializeClient() {
		super.onInitializeClient();
	}
}
