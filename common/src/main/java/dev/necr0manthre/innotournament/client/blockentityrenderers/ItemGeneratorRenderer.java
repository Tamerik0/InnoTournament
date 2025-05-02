package dev.necr0manthre.innotournament.client.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.necr0manthre.innotournament.blocks.ItemGeneratorBlock;
import dev.necr0manthre.innotournament.blocks.blockentities.ItemGeneratorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import static java.lang.Float.max;

public class ItemGeneratorRenderer implements BlockEntityRenderer<ItemGeneratorBlockEntity> {
	public ItemGeneratorRenderer(BlockEntityRendererProvider.Context context) {

	}

	@Override
	public void render(ItemGeneratorBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Vec3 vec3) {
		if (blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() instanceof ItemGeneratorBlock block) {
			float height = block.getItemHeight();
			int itemCount = 0;
			for (int i = 0; i < blockEntity.getContainerSize(); i++) {
				if (!blockEntity.getItem(i).isEmpty()) {
					itemCount = i + 1;
				}
			}
			float time = blockEntity.getLevel().getGameTime() + tickDelta;
			float localRotationSpeed = 4;
			float globalRotationSpeed = 1;
			if (itemCount == 1) {
				matrices.pushPose();
				double offset = Math.sin(time / 8.0 % (2 * Math.PI)) * 0.1;
				matrices.translate(0.5, height, 0.5);
				matrices.mulPose(Axis.YP.rotationDegrees(time * globalRotationSpeed % 360));
				matrices.translate(0, offset, 0);
				int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
				Minecraft.getInstance().getItemRenderer().renderStatic(blockEntity.getItem(0), ItemDisplayContext.GROUND, lightAbove, OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, blockEntity.getLevel(), 0);
				matrices.popPose();
			} else {
				float scale = max(1.15f - 0.15f * itemCount, 0.6f);
				for (int i = 0; i < itemCount; i++) {
					matrices.pushPose();
					double offset = Math.sin((time / 8.0) % (2 * Math.PI) + (i * 2 * Math.PI) / itemCount) * 0.1;
					matrices.translate(0.5, height, 0.5);
					matrices.mulPose(Axis.YP.rotationDegrees(time * globalRotationSpeed % 360 + (float) (i * 360) / itemCount));
					matrices.translate(0.5, 0, 0);
					matrices.mulPose(Axis.YP.rotationDegrees(time * localRotationSpeed % 360));
					matrices.scale(scale, scale, scale);
					matrices.translate(0, offset, 0);
					int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
					Minecraft.getInstance().getItemRenderer().renderStatic(blockEntity.getItem(i), ItemDisplayContext.GROUND, lightAbove, OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, blockEntity.getLevel(), 0);
					matrices.popPose();
				}
			}
		}
	}
}