package net.milkev.milkevsmultiblocklibrary.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.milkev.milkevsmultiblocklibrary.common.blockEntities.MultiBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MultiBlockEntityRenderer implements BlockEntityRenderer<MultiBlockEntity> {
    
    public MultiBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(MultiBlockEntity blockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        //only do stuff if the preview is enabled
        //CLIENT SYNCED DATA;
        //blockEntity.isRenderPreview() //if the preview should be rendered
        //blockEntity.getBlocksMissing() //holds lists of all the blocks needed to complete the structure
        //blockEntity.getDirection() //irrelevant here
        //blockEntity.isValid() //technically irrelevant here, but also needed since isRenderPreview uses it
        if(blockEntity.isRenderPreview()) {
            matrixStack.push();
            Map<BlockPos, List<Block>> blocksMissing = blockEntity.getBlocksMissing();

            blocksMissing.forEach(((blockPos, blocks) -> {
                float time = blockEntity.getWorld().getTime() + tickDelta;
                int timeFactor = 20;
                int selector = Math.min(Math.max(
                        (int) Math.round(((time%(blocks.size()*timeFactor))/timeFactor)+0.5)-1
                        , 0), blocks.size()-1);
                //System.out.println("Selected " + selector + " out of range " + blocks.size());
                Block block = blocks.get(selector);
                if(block == null) {
                    //System.out.println("Block was null!");
                    block = blocks.get(0);
                }
                if (block != null) {
                    //local if controller position is 0, 0, 0
                    BlockPos localRenderPos = blockPos.subtract(blockEntity.getPos());
                    matrixStack.translate(localRenderPos.getX(), localRenderPos.getY(), localRenderPos.getZ());
                    float scale = 0.5f;
                    matrixStack.translate(scale / 2, scale / 2, scale / 2);
                    matrixStack.scale(scale, scale, scale);
                    //potentially replace with render getter for the block? unsure how to do atm
                    //atm this means something using a block entity renderer (such as chests) will not render 
                    //and blocks with custom models (such as torches) may render a little funny
                    
                    //would also like to make render slightly transparent
                    
                    //if you know how to do either of these things let me know!
                    MinecraftClient.getInstance().getBlockRenderManager().renderBlock(block.getDefaultState(),
                            new BlockPos(0, 0, 0),
                            blockEntity.getWorld(),
                            matrixStack,
                            vertexConsumerProvider.getBuffer(RenderLayer.getTranslucent()),
                            true,
                            Random.create());
                    matrixStack.scale(1 / scale, 1 / scale, 1 / scale);
                    matrixStack.translate(-scale / 2, -scale / 2, -scale / 2);
                    matrixStack.translate(-localRenderPos.getX(), -localRenderPos.getY(), -localRenderPos.getZ());
                }
            }));


            // Mandatory call after GL calls
            matrixStack.pop();
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(MultiBlockEntity blockEntity) {
        return true;
    }
}
