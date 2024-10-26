package net.milkev.milkevsmultiblocklibrary.common.blockEntities;

import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExampleMultiblockEntity extends MultiBlockEntity implements BlockEntityTicker<ExampleMultiblockEntity> {
    
    public ExampleMultiblockEntity(BlockPos blockPos, BlockState blockState) {
        super(MilkevsMultiBlockLibrary.EXAMPLE_MULTIBLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void tick(World world, BlockPos blockPos, BlockState blockState, ExampleMultiblockEntity blockEntity) {
        
    }

    public ActionResult interact(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
        validateStructure();
        System.out.println(isStructureValid());
        return ActionResult.FAIL;
    }

    @Override
    protected Block[][][] getStructureMatrixPure() {
        return new Block[][][]{
                {
                        {Blocks.AIR, Blocks.NETHERITE_BLOCK, Blocks.AIR},
                        {Blocks.NETHERITE_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.NETHERITE_BLOCK},
                        {Blocks.AIR, Blocks.NETHERITE_BLOCK, Blocks.AIR}
                },
                {
                        {Blocks.AIR, MilkevsMultiBlockLibrary.EXAMPLE_MULTIBLOCK, Blocks.AIR},
                        {Blocks.LAPIS_BLOCK, Blocks.STRUCTURE_VOID, Blocks.LAPIS_BLOCK},
                        {Blocks.AIR, Blocks.LAPIS_BLOCK, Blocks.AIR}
                }
        };
    }

    @Override
    protected Block[][][][] getStructureMatrixList() {
        return new Block[][][][]{};
    }

    @Override
    protected boolean structureCanRotateVertically() {
        return false;
    }
}
