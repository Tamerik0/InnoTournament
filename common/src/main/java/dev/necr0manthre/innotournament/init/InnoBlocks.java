package dev.necr0manthre.innotournament.init;

import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.blocks.BookGenerator;
import dev.necr0manthre.innotournament.blocks.ItemGeneratorBlock;
import dev.necr0manthre.innotournament.blocks.NetherItemGenerator;
import dev.necr0manthre.innotournament.blocks.StandartItemGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class InnoBlocks {
	public static final ItemGeneratorBlock ITEM_GENERATOR_BLOCK = StandartItemGenerator.register();
	public static final ItemGeneratorBlock BOOK_GENERATOR_BLOCK = BookGenerator.register();
	public static final ItemGeneratorBlock NETHER_GENERATOR_BLOCK = NetherItemGenerator.register();

	public static void initialize() {
	}

	public static <T extends Block> T registerBlock(Function<BlockBehaviour.Properties, T> constructor, BlockBehaviour.Properties properties, String name) {
		var rl = ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, name);
		return Registry.register(BuiltInRegistries.BLOCK, rl, constructor.apply(properties.setId(ResourceKey.create(Registries.BLOCK, rl))));
	}

	public static <T extends Block> T registerBlock(Function<BlockBehaviour.Properties, T> constructor, String name) {
		return registerBlock(constructor, BlockBehaviour.Properties.of(), name);
	}
}
