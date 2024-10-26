package net.milkev.milkevsmultiblocklibrary.common;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.milkev.milkevsmultiblocklibrary.common.blockEntities.ExampleMultiblockEntity;
import net.milkev.milkevsmultiblocklibrary.common.blocks.ExampleMultiblock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class MilkevsMultiBlockLibrary implements ModInitializer {

	public static final String MOD_ID = "milkevsmultiblocklibrary";
	
	public static final ExampleMultiblock EXAMPLE_MULTIBLOCK = new ExampleMultiblock(FabricBlockSettings.create().strength(50));
	public static final BlockEntityType<ExampleMultiblockEntity> EXAMPLE_MULTIBLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ExampleMultiblockEntity::new, EXAMPLE_MULTIBLOCK).build();

	@Override
	public void onInitialize() {

		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "example_multiblock"), EXAMPLE_MULTIBLOCK);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "example_multiblock"), new BlockItem(EXAMPLE_MULTIBLOCK, new FabricItemSettings().rarity(Rarity.COMMON)));
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID + "example_multiblock_entity"), EXAMPLE_MULTIBLOCK_ENTITY);

		System.out.println(MOD_ID + " Initialized");
	}

}
