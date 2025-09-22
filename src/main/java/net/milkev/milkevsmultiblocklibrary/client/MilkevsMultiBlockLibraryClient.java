package net.milkev.milkevsmultiblocklibrary.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.milkev.milkevsmultiblocklibrary.client.render.MultiBlockEntityRenderer;
import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class MilkevsMultiBlockLibraryClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        
        //System.out.println(MilkevsMultiBlockLibrary.typeList);
        MilkevsMultiBlockLibrary.typeList.iterator().forEachRemaining(entry -> BlockEntityRendererFactories.register(entry, MultiBlockEntityRenderer::new));
        
        System.out.println(MilkevsMultiBlockLibrary.MOD_ID + " Client Initialized with " + MilkevsMultiBlockLibrary.typeList.size() + " multiblocks registered!");
    }
    
}
