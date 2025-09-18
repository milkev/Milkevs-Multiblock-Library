package net.milkev.milkevsmultiblocklibrary.common.example.blockEntites;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.milkev.milkevsmultiblocklibrary.common.blockEntities.MultiBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;

public class ExampleMultiblockEntity extends MultiBlockEntity {
    
    private int counter = 200;
    
    public ExampleMultiblockEntity(BlockPos blockPos, BlockState blockState) {
        super(MilkevsMultiBlockLibrary.EXAMPLE_MULTIBLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void ticker(World world, BlockPos bLockPos, BlockState blockState, MultiBlockEntity blockEntity) {
        //this is your ticking function
        //oooo look at me im ticckiinnnnggggg
    }
    
    public ActionResult interact(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, BlockHitResult blockHitResult) {
        //note that when the structure is validated is entirely up to you
        //id personally recommend just calling it every tick until it is valid, and then on a timer or when the structure tries to do something such as complete a recipe
        validateStructure();
        //you can call isStructureValid(); to ask if the structure thinks itself to be valid. This is a cached value from the last validateStructure() call.
        System.out.println(isStructureValid());
        //example function to output an item into any storages in the structure
        if(isStructureValid()) {
            depositItem();
        }
        //for now, controlling the preview is done with the interact, so if you want to toggle the preview, pass through super if you dont do anything
        return super.interact(blockState, world, blockPos, playerEntity, blockHitResult);
    }

    public Storage<ItemVariant> getInventory(BlockPos blockPos) {
        System.out.println("Looking for inventory at: " + blockPos);
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(this.world, blockPos, Direction.DOWN);
        return storage;
    }
    
    public void depositItem() {
        //loop through all blocks in structure
        for (Map.Entry<BlockPos, Block> entry : getBlocksInStructure().entrySet()) {
            //check if the block has an entity
            if (entry.getValue().getDefaultState().hasBlockEntity()) {
                //attempt to get the storage from the entity
                Storage<ItemVariant> storage = getInventory(entry.getKey());
                
                //insert an item into the storage
                if (storage != null) {
                    System.out.println("storage is real");
                    if (storage.supportsInsertion())
                        System.out.println("storage supports insertion");
                    try (Transaction transaction = Transaction.openOuter()) {
                        ItemStack toDeposit = new ItemStack(Items.ACACIA_BOAT);
                        storage.insert(ItemVariant.of(toDeposit), 1, transaction);
                        System.out.println("transaction complete!");
                        transaction.commit();
                    }

                }
            }
        }
    }
    

    @Override
    protected Block[][][][] getStructureMatrixList() {
        Block[] walls = new Block[]{Blocks.NETHERITE_BLOCK,Blocks.ANCIENT_DEBRIS};
        Block[] tips = new Block[]{Blocks.LAPIS_BLOCK,Blocks.GOLD_BLOCK,Blocks.IRON_BLOCK};
        Block[] core = new Block[]{MilkevsMultiBlockLibrary.EXAMPLE_MULTIBLOCK};
        Block[] air = new Block[]{Blocks.AIR};
        Block[] sVoid = new Block[]{Blocks.STRUCTURE_VOID};
        Block[] chest = new Block[]{Blocks.CHEST,Blocks.BARREL};
        return new Block[][][][]{
                {
                        {air, walls, air},
                        {walls, walls, walls},
                        {air, walls, air}
                },
                {
                        {air, tips, air},
                        {tips, chest, tips},
                        {air, core, air}
                }
        };
    }

    @Override
    protected boolean structureCanRotateVertically() {
        return false;
    }


}
