package net.milkev.milkevsmultiblocklibrary.common.items;

import net.milkev.milkevsmultiblocklibrary.common.blockEntities.MultiBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class MultiBlockBuilder extends Item {
    public MultiBlockBuilder(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext itemUsageContext) {
        if(itemUsageContext.getWorld().isClient() 
                || itemUsageContext.getPlayer() == null) {
            return ActionResult.PASS;
        }
        ServerWorld world = (ServerWorld) itemUsageContext.getWorld();
        BlockEntity blockEntity = world.getBlockEntity(itemUsageContext.getBlockPos());
        if(blockEntity instanceof MultiBlockEntity multiBlockEntity) {
            //if structure is valid, dont do anything
            //System.out.println("MultiBlockBuilder::UseOnBlock - begin");
            if(multiBlockEntity.isStructureValid()) {
                return ActionResult.PASS;
            }
            //if player crouching, change direction
            if(itemUsageContext.getPlayer().isSneaking()) {
                //System.out.println("Rotating!");
                multiBlockEntity.setDirection(multiBlockEntity.getDirection().rotateYClockwise());
                //System.out.println("Direction set to: " + multiBlockEntity.getDirection());
                //System.out.println("Validating!");
                multiBlockEntity.validateStructureDirectional(multiBlockEntity.getDirection());
                //System.out.println("Rotated!");
                return ActionResult.CONSUME;
            }
            
            multiBlockEntity.validateStructureDirectional(multiBlockEntity.getDirection());
            
            Map<BlockPos, List<Block>> blocksMissing = multiBlockEntity.getBlocksMissing();
            Map.Entry<BlockPos, List<Block>> blocksMissingEntry = blocksMissing.entrySet().iterator().next();
            
            BlockPos checkLocation = blocksMissingEntry.getKey();
            
            List<Block> playerDunHaveDis = new ArrayList<>();
            boolean canPlace = false;
            
            for (Block block : blocksMissingEntry.getValue()) {
                if(block != null) {
                    if(world.canPlace(block.getDefaultState(), checkLocation, ShapeContext.absent()) 
                            && world.getBlockState(checkLocation).canReplace(new ItemPlacementContext(itemUsageContext))) {
                        
                        canPlace = true;
                        PlayerInventory playerInventory = itemUsageContext.getPlayer().getInventory();
                        //if player has the item, or if they are in creative
                        if (playerInventory.contains(block.asItem().getDefaultStack()) || itemUsageContext.getPlayer().isCreative()) {
                            world.setBlockState(checkLocation, block.getDefaultState());
                            //if in creative, dont consume block
                            if (!itemUsageContext.getPlayer().isCreative()) {
                                playerInventory.getStack(playerInventory.getSlotWithStack(block.asItem().getDefaultStack())).decrement(1);
                            }
                            //System.out.println("Placed " + block + " at " + checkLocation);
                            //use method to update preview
                            multiBlockEntity.validateStructureDirectional(multiBlockEntity.getDirection());
                            return ActionResult.SUCCESS;
                        } else {
                            playerDunHaveDis.add(block);
                        }
                    }
                }
            }
            
            if(canPlace) {
                itemUsageContext.getPlayer().sendMessage(Text.of("Missing Block: " + playerDunHaveDis), true);
            } else {
                itemUsageContext.getPlayer().sendMessage(Text.of("Unable to place block at: " + checkLocation), true);
            }
            
            
        }
        return ActionResult.PASS;
    }
    
    @Override
    public void appendTooltip(ItemStack itemStack, TooltipContext tooltipContext, List<Text> tooltip, TooltipType tooltipType) {
        tooltip.add(Text.translatable("item.milkevsmultiblocklibrary.multiblock_builder.desc_rotate"));
        tooltip.add(Text.translatable("item.milkevsmultiblocklibrary.multiblock_builder.desc_place"));
        tooltip.add(Text.translatable("item.milkevsmultiblocklibrary.multiblock_builder.desc_preview"));
    }
    
}
