package dev.necr0manthre.innotournament.blocks;

import dev.necr0manthre.innotournament.init.InnoBlocks;
import dev.necr0manthre.innotournament.init.InnoItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BookGenerator extends ItemGeneratorBlock {

	public BookGenerator(Properties properties) {
		super("inno/bookGen.json", properties);
	}

	public static ItemGeneratorBlock register() {
		return InnoBlocks.registerBlock(BookGenerator::new, "book_generator");
	}

	public static BlockItem registerBlockItem() {
		InnoItems.setItemCreativeTab(() -> InnoItems.BOOK_GENERATOR_BLOCK_ITEM, CreativeModeTabs.FUNCTIONAL_BLOCKS);
		return InnoItems.registerBlockItem(InnoBlocks.BOOK_GENERATOR_BLOCK, "book_generator");
	}

	protected VoxelShape makeShape() {
		return Shapes.or(
				Shapes.box(0.125, 0, 0.125, 0.875, 0.375, 0.875),
				Shapes.box(0.3125, 0.375, 0.1875, 0.6875, 0.5, 0.8125),
				Shapes.box(0.6875, 0.375, 0.3125, 0.8125, 0.5, 0.6875),
				Shapes.box(0.1875, 0.375, 0.3125, 0.3125, 0.5, 0.6875),
				Shapes.box(0.6875, 0.375, 0.6875, 0.8125, 0.4375, 0.8125),
				Shapes.box(0.1875, 0.375, 0.1875, 0.3125, 0.4375, 0.3125),
				Shapes.box(0.1875, 0.375, 0.6875, 0.3125, 0.4375, 0.8125),
				Shapes.box(0.34375, 0.5, 0.34375, 0.65625, 0.53125, 0.65625),
				Shapes.box(0.6875, 0.375, 0.1875, 0.8125, 0.4375, 0.3125),
				Shapes.box(0.6875, 0.4375, 0.25, 0.75, 0.5625, 0.3125),
				Shapes.box(0.25, 0.4375, 0.25, 0.3125, 0.5625, 0.3125),
				Shapes.box(0.25, 0.4375, 0.6875, 0.3125, 0.5625, 0.75),
				Shapes.box(0.6875, 0.4375, 0.6875, 0.75, 0.5625, 0.75)
		);
	}

	@Override
	public float getItemHeight() {
		return 0.6f;
	}
}
