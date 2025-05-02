package dev.necr0manthre.innotournament.init.fabric;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class InnoBlockEntitiesImpl {
	public static <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BlockEntityType.BlockEntitySupplier<B> constructor, Block... validBlocks) {
		return FabricBlockEntityTypeBuilder.create(constructor::create, validBlocks).build();
	}
}
