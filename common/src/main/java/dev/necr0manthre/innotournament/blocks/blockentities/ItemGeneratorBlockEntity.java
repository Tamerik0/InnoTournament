package dev.necr0manthre.innotournament.blocks.blockentities;


import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.hooks.block.BlockEntityHooks;
import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.blocks.ItemGeneratorBlock;
import dev.necr0manthre.innotournament.init.InnoBlockEntities;
import dev.necr0manthre.innotournament.init.InnoBlocks;
import dev.necr0manthre.innotournament.init.InnoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemGeneratorBlockEntity extends BlockEntity implements Container {
    List<ItemGeneratorBlock.LevelData> data;
    int level = 0;
    boolean readyForNextLevel = false;
    private static HashMap<UUID, ItemGeneratorBlockEntity> forcePvp = new HashMap<>();

    public static boolean checkForcePvp(ServerPlayer player) {
        return forcePvp.getOrDefault(player.getUUID(), null) != null;
    }

    public ItemStack generateItem() {
        float g = 0;
        var random = new Random();
        for (var stack : data.get(level).probabilities.keySet()) {
            if (random.nextDouble() <= data.get(level).probabilities.get(stack) / (1 - g)) {
                return stack.copy();
            }
            g += data.get(level).probabilities.get(stack);
        }
        return null;
    }

    private int timer;
    private int timer2 = 0;
    private final NonNullList<ItemStack> items;

    public ItemGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(InnoBlockEntities.ITEM_GENERATOR_BLOCK_ENTITY_TYPE, pos, state);
        data = ((ItemGeneratorBlock) state.getBlock()).data;
        items = NonNullList.withSize(10, ItemStack.EMPTY);
    }

    public static BlockEntityType<ItemGeneratorBlockEntity> register() {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, "item_generator_block_entity"), InnoBlockEntities.createBlockEntityType(ItemGeneratorBlockEntity::new, InnoBlocks.ITEM_GENERATOR_BLOCK, InnoBlocks.BOOK_GENERATOR_BLOCK, InnoBlocks.NETHER_GENERATOR_BLOCK));
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider wrapper) {
        super.saveAdditional(nbt, wrapper);
        ContainerHelper.saveAllItems(nbt, items, wrapper);
        nbt.putInt("timer", timer);
        nbt.putBoolean("readyForNextLevel", readyForNextLevel);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider wrapper) {
        super.loadAdditional(nbt, wrapper);
        clearContent();
        ContainerHelper.loadAllItems(nbt, items, wrapper);
        timer = nbt.getInt("timer").orElseThrow();
        readyForNextLevel = nbt.getBoolean("readyForNextLevel").orElseThrow();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, ItemGeneratorBlockEntity blockEntity) {
        blockEntity.tick(world, pos, state);
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            if (!data.isEmpty()) {
                if (timer2 >= 20) {
//                    if (!InnoPersistentData.getServerState(world.getServer()).isPvpEnabled() && InnoPersistentData.getServerState(world.getServer()).getPhase() == 1) {
                    for (var player : world.getServer().getPlayerList().getPlayers()) {
                        boolean v = player.blockPosition().getCenter().distanceTo(pos.getCenter()) <= 5;
                        boolean p = checkForcePvp(player);
                        if (p && !v) {
                            if (forcePvp.get(player.getUUID()) == this) {
                                forcePvp.put(player.getUUID(), null);
                            } else {
                                continue;
                            }
                        } else if (!p && v) {
                            forcePvp.put(player.getUUID(), this);

                        } else {
                            continue;
                        }
                        player.sendSystemMessage(Component.literal(v ? "You entered area around generator. Now pvp is enabled for you." :
                                "You left area around generator. Now pvp is disabled for you."));
                    }
                    timer2 = 0;

                } else {
                    timer2++;
                }
                if (data.size() <= level + 1)
                    readyForNextLevel = false;
                if (readyForNextLevel) {
                    if (timer >= data.get(level + 1).generationTime / Innotournament.getGeneratorSpeed(world.getServer())) {
                        clearContent();
                        level++;
                        timer = 0;
                        setItem(0, generateItem());
                        readyForNextLevel = false;
                    } else {
                        timer++;
                    }
                } else if (timer >= data.get(level).generationTime / Innotournament.getGeneratorSpeed(world.getServer())) {
                    timer = 0;
                    for (int i = 0; i < data.get(level).maxItemCount; i++) {
                        if (items.get(i).isEmpty()) {
                            setItem(i, generateItem());
                            break;
                        }
                    }
                    if (!getItem(data.get(level).maxItemCount - 1).isEmpty() && level + 1 < data.size()) {
                        readyForNextLevel = true;
                    }

                } else {
                    timer++;
                }
                setChanged();
                world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithFullMetadata(registryLookup);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch((item) -> item == ItemStack.EMPTY);
    }


    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }
    }

    public InteractionResult playerRMB(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            if (player.getMainHandItem().getItem() == InnoItems.INNO_DICE) {
                player.getInventory().removeItem(player.getInventory().getSelectedSlot(), 1);
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isEmpty())
                        break;
                    setItem(i, generateItem());
                }
            } else {
                if (!isEmpty()) {
                    level = 0;
                    timer = 0;
                    readyForNextLevel = false;
                }
                for (int i = 0; i < items.size(); i++) {
                    player.getInventory().placeItemBackInInventory(getItem(i));
                    removeItemNoUpdate(i);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
