package dev.necr0manthre.innotournament.blocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.blocks.blockentities.ItemGeneratorBlockEntity;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public abstract class ItemGeneratorBlock extends BaseEntityBlock {
	public List<LevelData> data;
	protected VoxelShape SHAPE = makeShape();
	String jsonDataPath;

	public ItemGeneratorBlock(String jsonDataPath, BlockBehaviour.Properties properties) {
		super(properties.destroyTime(-1).strength(99999999));
		this.jsonDataPath = jsonDataPath;
	}

	public void Init(HolderLookup.Provider provider) {
		data = new ArrayList<>();
		JsonArray d;
		var path = Platform.getConfigFolder().resolve(jsonDataPath);
		try {

			d = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonArray();
			for (var i : d) {
//				if( Minecraft.getInstance().getSingleplayerServer() != null) {
					var lvlData = LevelData.load(i.getAsJsonObject(), provider);
					data.add(lvlData);
//				}
			}
		} catch (Exception e) {
			try {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
				Files.writeString(path, "[]");
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public float getItemHeight() {
		return 1;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ItemGeneratorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, InnoBlockEntities.ITEM_GENERATOR_BLOCK_ENTITY_TYPE, ItemGeneratorBlockEntity::tick);
	}

	@Override
	protected MapCodec<? extends ItemGeneratorBlock> codec() {
		return null;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		var blockEntity = (ItemGeneratorBlockEntity) (world.getBlockEntity(pos));
		return blockEntity.playerRMB(state, world, pos, player, hit);
	}


	protected abstract VoxelShape makeShape();

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	public static class LevelData {
		public Map<ItemStack, Float> probabilities;
		public int generationTime;
		public int maxItemCount;

		private LevelData() {}
		public static LevelData load(JsonObject json, HolderLookup.Provider provider) {
			var gt = json.getAsJsonPrimitive("generationTime").getAsInt();
			var mic = json.getAsJsonPrimitive("maxItemCount").getAsInt();
			var prob = json.getAsJsonObject("probabilities").asMap();
			var prob1 = new Hashtable<ItemStack, Float>();
			float s = 0;
			for (var j : prob.keySet()) {
				try {
					ItemStack itemStack;
					try {
//						var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
						itemStack = ItemStack.parse(provider, NbtUtils.snbtToStructure(j)).orElseThrow();
					} catch (Exception e) {
						itemStack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(j)).orElse(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:air")).orElseThrow()).value().getDefaultInstance();
					}
					prob1.put(itemStack, prob.get(j).getAsFloat());
					s += prob.get(j).getAsFloat();
				} catch (Exception ex) {
					Innotournament.LOGGER.info(ex.getMessage());
					Innotournament.LOGGER.info(j);
				}
			}
			var data = new LevelData();
			data.generationTime = gt;
			data.maxItemCount = mic;
			data.probabilities = new HashMap<>();
			for (var k : prob1.keySet()) {
				data.probabilities.put(k, prob1.get(k) / s);
			}
			return data;
		}
	}
}
