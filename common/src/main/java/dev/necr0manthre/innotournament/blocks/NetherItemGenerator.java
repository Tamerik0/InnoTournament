package dev.necr0manthre.innotournament.blocks;


import dev.necr0manthre.innotournament.init.InnoBlocks;
import dev.necr0manthre.innotournament.init.InnoItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherItemGenerator extends ItemGeneratorBlock {
	public NetherItemGenerator(Properties properties) {
		super("inno/netherGen.json", properties);
	}

	public static ItemGeneratorBlock register() {
		return InnoBlocks.registerBlock(NetherItemGenerator::new, "nether_generator");
	}

	public static BlockItem registerBlockItem() {
		InnoItems.setItemCreativeTab(() -> InnoItems.NETHER_GENERATOR_BLOCK_ITEM, CreativeModeTabs.FUNCTIONAL_BLOCKS);
		return InnoItems.registerBlockItem(InnoBlocks.NETHER_GENERATOR_BLOCK, "nether_generator");
	}

	public VoxelShape makeShape() {
		return Shapes.or(
				Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.75, 0.8125),
				Shapes.box(0.1875, 0, 0.0625, 0.8125, 0.0625, 0.1875),
				Shapes.box(0.1875, 0, 0.8125, 0.8125, 0.0625, 0.9375),
				Shapes.box(0.0625, 0, 0.0625, 0.1875, 0.0625, 0.9375),
				Shapes.box(0.8125, 0.0625, 0.125, 0.875, 0.125, 0.875),
				Shapes.box(0.125, 0.0625, 0.125, 0.1875, 0.125, 0.875),
				Shapes.box(0.1875, 0.0625, 0.125, 0.8125, 0.125, 0.1875),
				Shapes.box(0.1875, 0.0625, 0.8125, 0.8125, 0.125, 0.875),
				Shapes.box(0.8125, 0.6875, 0.0625, 0.9375, 0.75, 0.9375),
				Shapes.box(0.8125, 0.625, 0.125, 0.875, 0.6875, 0.875),
				Shapes.box(0.125, 0.625, 0.125, 0.1875, 0.6875, 0.875),
				Shapes.box(0.1875, 0.625, 0.125, 0.8125, 0.6875, 0.1875),
				Shapes.box(0.1875, 0.625, 0.8125, 0.8125, 0.6875, 0.875),
				Shapes.box(0.0625, 0.6875, 0.0625, 0.1875, 0.75, 0.9375),
				Shapes.box(0.1875, 0.6875, 0.0625, 0.8125, 0.75, 0.1875),
				Shapes.box(0.1875, 0.6875, 0.8125, 0.8125, 0.75, 0.9375),
				Shapes.box(0.25, 0.75, 0.25, 0.75, 0.78125, 0.75),
				Shapes.box(0.25, 0.75, 0.875, 0.75, 0.8125, 0.9375),
				Shapes.box(0.25, 0.75, 0.0625, 0.75, 0.8125, 0.125),
				Shapes.box(0.875, 0.75, 0.25, 0.9375, 0.8125, 0.75),
				Shapes.box(0.0625, 0.75, 0.25, 0.125, 0.8125, 0.75),
				Shapes.box(0.8125, 0, 0.0625, 0.9375, 0.0625, 0.9375)
		);
	}

	@Override
	public float getItemHeight() {
		return 1;
	}
}
