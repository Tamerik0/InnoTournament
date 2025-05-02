package dev.necr0manthre.innotournament.init;


import dev.necr0manthre.innotournament.Innotournament;
import dev.necr0manthre.innotournament.blocks.BookGenerator;
import dev.necr0manthre.innotournament.blocks.NetherItemGenerator;
import dev.necr0manthre.innotournament.blocks.StandartItemGenerator;
import dev.necr0manthre.innotournament.items.InnoDice;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.Supplier;

public class InnoItems {
	public static final Item INNO_DICE = registerItem(InnoDice::new, "inno_dice");

	public static void initialize() {
		setItemCreativeTab(() -> INNO_DICE, CreativeModeTabs.FUNCTIONAL_BLOCKS);
	}

	public static <T extends Item> T registerItem(Function<Item.Properties, T> constructor, Item.Properties properties, String name) {
		var rl = ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, name);
		return Registry.register(BuiltInRegistries.ITEM, rl, constructor.apply(properties.setId(ResourceKey.create(Registries.ITEM, rl))));
	}

	public static <T extends Item> T registerItem(Function<Item.Properties, T> constructor, String name) {
		return registerItem(constructor, new Item.Properties(), name);
	}

	public static <T extends Block> BlockItem registerBlockItem(T block, String name, Item.Properties settings) {
		return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, name), new BlockItem(block, settings));
	}

	public static <T extends Block> BlockItem registerBlockItem(T block, String name) {
		var rl = ResourceLocation.fromNamespaceAndPath(Innotournament.MOD_ID, name);
		return Registry.register(BuiltInRegistries.ITEM, rl, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, rl))));
	}

	public static void setItemCreativeTab(Supplier<Item> supplier, ResourceKey<CreativeModeTab> tab) {
//        ItemGroupEvents.modifyEntriesEvent(tab).register((itemGroup) -> itemGroup.accept(supplier.get()));
	}

	public static final BlockItem ITEM_GENERATOR_BLOCK_ITEM = StandartItemGenerator.registerBlockItem();
	public static final BlockItem BOOK_GENERATOR_BLOCK_ITEM = BookGenerator.registerBlockItem();
	public static final BlockItem NETHER_GENERATOR_BLOCK_ITEM = NetherItemGenerator.registerBlockItem();

}
