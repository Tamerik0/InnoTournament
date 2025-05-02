package dev.necr0manthre.innotournament.init;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.blocks.blockentities.ItemGeneratorBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class InnoBlockEntities {
    public static void initialize() {
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType.BlockEntitySupplier<T> constructor, Block block, String name) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, name), createBlockEntityType(constructor, block));
    }
    @ExpectPlatform
    public static <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BlockEntityType.BlockEntitySupplier<B> constructor, Block... validBlocks){
        throw new UnsupportedOperationException();
    };
    public static BlockEntityType<ItemGeneratorBlockEntity> ITEM_GENERATOR_BLOCK_ENTITY_TYPE = ItemGeneratorBlockEntity.register();
}
