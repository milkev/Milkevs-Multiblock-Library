package net.milkev.milkevsmultiblocklibrary.common;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.milkev.milkevsmultiblocklibrary.common.example.blockEntites.ExampleMultiblockEntity;
import net.milkev.milkevsmultiblocklibrary.common.blockEntities.MultiBlockEntity;
import net.milkev.milkevsmultiblocklibrary.common.example.blocks.ExampleMultiblock;
import net.milkev.milkevsmultiblocklibrary.common.items.MultiBlockBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

public class MilkevsMultiBlockLibrary implements ModInitializer {

	public static final String MOD_ID = "milkevsmultiblocklibrary";
	
	public static ExampleMultiblock EXAMPLE_MULTIBLOCK = null;
	public static BlockEntityType<ExampleMultiblockEntity> EXAMPLE_MULTIBLOCK_ENTITY = null;
	
	public static MultiBlockBuilder MULTIBLOCK_BUILDER = new MultiBlockBuilder(new Item.Settings().maxCount(1).rarity(Rarity.RARE));
	
	public static List<BlockEntityType<? extends MultiBlockEntity>> typeList = new ArrayList<>();
	
	@Override
	public void onInitialize() {

		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
		
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "multiblock_builder"), MULTIBLOCK_BUILDER);

		if(config.ExampleMultiblock) {
			EXAMPLE_MULTIBLOCK = new ExampleMultiblock(AbstractBlock.Settings.create().strength(50));
			EXAMPLE_MULTIBLOCK_ENTITY = BlockEntityType.Builder.create(
			ExampleMultiblockEntity::new, EXAMPLE_MULTIBLOCK).build();
			Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "example_multiblock"), EXAMPLE_MULTIBLOCK);
			Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "example_multiblock"), new BlockItem(EXAMPLE_MULTIBLOCK, new Item.Settings().rarity(Rarity.COMMON)));
			registerMultiblock(EXAMPLE_MULTIBLOCK_ENTITY, Identifier.of(MOD_ID, "example_multiblock"));
		}
		
		System.out.println(MOD_ID + " Initialized");
	}
	
	/*
	Idk another way to do this, so you'll have to register your multiblock blockEntity with this.
	If you dont do this, things should work fine, but there wont be a build preview for your structure. theoretically things shouldnt break but tbh i havent tested
	optional second method below if you dont want it to call the Registry.register function
	 */
	public void registerMultiblock(BlockEntityType<? extends MultiBlockEntity> blockEntityType, Identifier id) {
		typeList.add(blockEntityType);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, id, blockEntityType);
	}
	public void registerMultiblock(BlockEntityType<? extends MultiBlockEntity> blockEntityType) {
		typeList.add(blockEntityType);
		
	}

}
