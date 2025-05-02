package dev.necr0manthre.innotournament.blocks;

import dev.necr0manthre.innotournament.init.InnoBlocks;
import dev.necr0manthre.innotournament.init.InnoItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StandartItemGenerator extends ItemGeneratorBlock{
    public StandartItemGenerator(Properties properties) {
        super("inno/itemGen.json", properties);
    }

    public VoxelShape makeShape() {
        return Shapes.or(
                Shapes.box(0.125, 0, 0.125, 0.875, 0.375, 0.875),
                Shapes.box(0.46875, 0.5, 0.8125, 0.53125, 0.625, 0.875),
                Shapes.box(0.3125, 0.375, 0.125, 0.6875, 0.5, 0.875),
                Shapes.box(0.6875, 0.375, 0.3125, 0.875, 0.5, 0.6875),
                Shapes.box(0.125, 0.375, 0.3125, 0.3125, 0.5, 0.6875),
                Shapes.box(0.6875, 0.375, 0.6875, 0.8125, 0.5, 0.8125),
                Shapes.box(0.1875, 0.375, 0.1875, 0.3125, 0.5, 0.3125),
                Shapes.box(0.1875, 0.375, 0.6875, 0.3125, 0.5, 0.8125),
                Shapes.box(0.34375, 0.5, 0.34375, 0.65625, 0.53125, 0.65625),
                Shapes.box(0.6875, 0.375, 0.1875, 0.8125, 0.5, 0.3125),
                Shapes.box(0.8125, 0.5, 0.46875, 0.875, 0.625, 0.53125),
                Shapes.box(0.125, 0.5, 0.46875, 0.1875, 0.625, 0.53125),
                Shapes.box(0.46875, 0.5, 0.125, 0.53125, 0.625, 0.1875)
        );
    }
    public static ItemGeneratorBlock register() {
        return InnoBlocks.registerBlock(StandartItemGenerator::new, "item_generator");
    }

    public static BlockItem registerBlockItem() {
        InnoItems.setItemCreativeTab(() -> InnoItems.ITEM_GENERATOR_BLOCK_ITEM, CreativeModeTabs.FUNCTIONAL_BLOCKS);
        return InnoItems.registerBlockItem(InnoBlocks.ITEM_GENERATOR_BLOCK, "item_generator");
    }

    @Override
    public float getItemHeight() {
        return 0.7f;
    }
}
