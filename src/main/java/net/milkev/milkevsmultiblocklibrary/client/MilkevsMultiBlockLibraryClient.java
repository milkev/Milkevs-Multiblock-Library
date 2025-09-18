package net.milkev.milkevsmultiblocklibrary.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.milkev.milkevsmultiblocklibrary.client.render.MultiBlockEntityRenderer;
import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.milkev.milkevsmultiblocklibrary.common.blockEntities.MultiBlockEntity;
import net.milkev.milkevsmultiblocklibrary.common.utils.MultiBlockEntityComponent;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class MilkevsMultiBlockLibraryClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        
        //System.out.println(MilkevsMultiBlockLibrary.typeList);
        MilkevsMultiBlockLibrary.typeList.iterator().forEachRemaining(entry -> BlockEntityRendererFactories.register(entry, MultiBlockEntityRenderer::new));

        ClientPlayNetworking.registerGlobalReceiver(MultiBlockEntityComponent.MultiBlockEntityPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if(context.client().world.getBlockEntity(payload.blockEntityPos()) instanceof MultiBlockEntity multiBlockEntity) {
                    multiBlockEntity.applyPayload(payload);
                }
            });
        });
        
        
        
        System.out.println(MilkevsMultiBlockLibrary.MOD_ID + " Client Initialized");
    }
    
}
