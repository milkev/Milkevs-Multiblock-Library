package net.milkev.milkevsmultiblocklibrary.common.example.blocks;

import com.mojang.serialization.MapCodec;
import net.milkev.milkevsmultiblocklibrary.common.example.blockEntites.ExampleMultiblockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ExampleMultiblock extends BlockWithEntity implements BlockEntityProvider {
    public ExampleMultiblock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, BlockHitResult blockHitResult) {
        if(world.isClient) {return ActionResult.SUCCESS;}
        ExampleMultiblockEntity exampleBlockEntity = (ExampleMultiblockEntity) world.getBlockEntity(blockPos);

        return exampleBlockEntity.interact(blockState, world, blockPos, playerEntity, blockHitResult);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof BlockEntityTicker blockEntityTicker) {
                blockEntityTicker.tick(world1, pos, state1, blockEntity);
            }
        };
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ExampleMultiblockEntity(blockPos, blockState);
    }
}
