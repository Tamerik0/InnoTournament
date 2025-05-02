package dev.necr0manthre.innotournament.init.neoforge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class InnoBlockEntitiesImpl {
	public static <B extends BlockEntity> BlockEntityType<B> createBlockEntityType(BlockEntityType.BlockEntitySupplier<B> constructor, Block... validBlocks) {
		return new BlockEntityType<>(constructor, validBlocks);
	}
}
