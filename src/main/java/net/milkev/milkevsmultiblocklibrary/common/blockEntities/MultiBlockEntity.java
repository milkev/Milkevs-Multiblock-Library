package net.milkev.milkevsmultiblocklibrary.common.blockEntities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.milkev.milkevsmultiblocklibrary.common.utils.MultiBlockEntityComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public abstract class MultiBlockEntity extends BlockEntity implements BlockEntityTicker<MultiBlockEntity> {

    //if the structure is valid, use isStructureValid() to get this value.
    private boolean valid;
    //the location of the controller or "this". this is local 
    //synced to client
    private BlockPos controllerBlockPos;
    //this holds a map/list of all the blocks that are in the structure. If you need a reference to particular blocks or block types (for instance, IO blocks, upgrade blocks, etc) search this map for them
    //use getBlocksInStructure to get this value.
    private final Map<BlockPos, Block> blocksInStructure = new HashMap<>();
    //used to keep track of what blocks are missing from the structure. used for the multiblock builder & render preview
    //synced to client
    private Map<BlockPos, List<Block>> blocksMissing = new LinkedHashMap<>();
    //the actual direction is completely irrelevant since the multiblock cannot know what direction it "faces" without extra explicit statement of direction by the user. Instead this simply serves to be a control for what direction the multiblock builder attempts to build the structure in, which may or may not be the direction the structure actually "faces"
    private Direction direction = Direction.NORTH;
    //will be used to control wether or not the preview is rendered... if i can get the preview to render, anyway
    private boolean renderPreview = false;
    private MultiBlockEntityComponent.MultiBlockEntityPayload payload;
    
    public MultiBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
    
    //enable or disable the preview
    //i would like to move this feature to a gui element or as part of the multiblock builder tool, or just maybe have it constantly be displayed wouldnt be too bad.
    public ActionResult interact(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, BlockHitResult blockHitResult) {
        if(playerEntity.isSneaking() && playerEntity.getMainHandStack().isEmpty() && playerEntity.getOffHandStack().isEmpty()) {
            renderPreview = !renderPreview;
            //System.out.println("Set render preview of " + this + " to " + renderPreview);
            updateClient();
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }
    
    //use this instead of tick()
    //if your block entity does not tick, the preview will never work
    //(but lets be real if you are making a multiblock, its gonna tick)
    //other than not having to do implements BlockEntityTicker and using this method, your implementation of ticking will be exactly the same
    public abstract void ticker(World world, BlockPos bLockPos, BlockState blockState, MultiBlockEntity blockEntity);
    
    //This is used to update the client so that the render preview shows up properly.
    //set to only ever be able to send one per tick cus the encoder would crash if multiple were sent in the same tick. If you know another way to do this let me know since i hate doing this lol
    @Override
    public final void tick(World world, BlockPos blockPos, BlockState blockState, MultiBlockEntity blockEntity) {
        if(payload != null) {
            //System.out.println("Sending packet with info;");
            //System.out.println("Payload: " + payload);
            for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                if (ServerPlayNetworking.canSend(player, MultiBlockEntityComponent.MultiBlockEntityPayload.ID)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
            payload = null;
        } else {
            //System.out.println("no payload to send");
        }
        ticker(world, blockPos, blockState, blockEntity);
    }
    
    //this is used by the builder so it can get an updated blocksMissing in order to build the structure
    //validateStructure loops this to check all 4 different directions
    //if you want to call this manually for some reason, set setMissing to false as it should only be set to true when the builder is calling this
    public boolean validateStructureDirectional(Direction dir) {
        Block[][][][] rotatedStructureMatrixList = getRotatedStructureMatrixList(dir);
        controllerBlockPos = getStructureOffsetList(rotatedStructureMatrixList);
        return validateStructureList(rotatedStructureMatrixList, dir);
    }
    
    //call this to update if the structure is valid
    public void validateStructure() {
        if(!Objects.requireNonNull(getWorld()).isClient()) {
            if(getStructureMatrixList().length > 0) {
                if(validateStructureDirectional(Direction.NORTH)) {
                    valid = true;
                    return;
                }if(validateStructureDirectional(Direction.EAST)) {
                    valid = true;
                    return;
                }if(validateStructureDirectional(Direction.SOUTH)) {
                    valid = true;
                    return;
                }if(validateStructureDirectional(Direction.WEST)) {
                    valid = true;
                    return;
                }
            }
        }
        valid = false;
    }

    //the actual function to validate the structure
    //updates blocksMissing if dir is equal to this.direction
    private boolean validateStructureList(Block[][][][] structureMatrixList, Direction dir) {
        blocksInStructure.clear();
        if(dir == this.direction) {
            blocksMissing.clear();
        }
        boolean check = true;
        for (int y = 0; y < structureMatrixList.length; y++) {
            Block[][][] yMatrix = structureMatrixList[y];
            for (int x = 0; x < yMatrix.length; x++) {
                Block[][] xMatrix = yMatrix[x];
                for (int z = 0; z < xMatrix.length; z++) {
                    Block[] blocks = xMatrix[z];
                    BlockPos checkLocation = new BlockPos(x, y ,z);
                    if(!checkBlocks(checkLocation, blocks)) {
                        //System.out.println("Structure Invalid!");
                        check = false;
                        if(dir == this.direction) {
                            blocksMissing.put(getGlobalBlockPosFromLocal(checkLocation), List.of(blocks));
                        }
                    } else {
                        blocksInStructure.put(getGlobalBlockPosFromLocal(checkLocation), world.getBlockState(getGlobalBlockPosFromLocal(checkLocation)).getBlock());
                    }
                }
            }
        }
        if(this.direction == dir) {
            updateClient();
        }
        //System.out.println("Structure Valid!");
        return check;
    }

    //local -> global block pos
    public BlockPos getGlobalBlockPosFromLocal(BlockPos blockPos) {
        //System.out.println("Controller Block Pos:" + controllerBlockPos + "\nblockPos:" + blockPos + "\nthis.pos: " + this.pos);
        return this.pos.subtract(controllerBlockPos).add(blockPos);
    }
    
    //check if a block is present at a location
    private boolean checkBlock(BlockPos blockPosLocal, Block block) {
        //System.out.println("Found block: " + getWorld().getBlockState(getLocalBlockPos(blockPos)).getBlock() + " at: " + getLocalBlockPos(blockPos) + " and wanted block: " + block);
        if(block == Blocks.AIR) {
            return true;
        }
        
        Block inWorldBlock = getWorld().getBlockState(getGlobalBlockPosFromLocal(blockPosLocal)).getBlock();
        
        if(block == Blocks.STRUCTURE_VOID) {
            return inWorldBlock == Blocks.AIR;
        }
        if(inWorldBlock == block) {
            return true;
        }
        return false;
    }
    
    //for loop checkBlock
    private boolean checkBlocks(BlockPos blockPosLocal, Block[] blocks) {
        for(int i = 0; i < blocks.length; i++) {
            if(checkBlock(blockPosLocal, blocks[i])) {
                return true;
            }
        }
        return false;
    }
    
    //rotate the structure matrix
    //vertical not implemented yet
    private Block[][][][] getRotatedStructureMatrixList(Direction dir) {
        int rotations = -1;
        switch(dir) {
            case NORTH -> rotations = 0; 
            case EAST -> rotations = 1;
            case SOUTH -> rotations = 2;
            case WEST -> rotations = 3;
            case UP, DOWN -> {
                rotations = 0;
                System.out.println("MilkevsMultiBlockLibrary::MultiBlockEntity::getRotatedStructureMatrixList - attempted to rotate vertically! apologies, this isnt implemented yet!" + dir);
            }
        }
        if(rotations == -1) {
            System.out.println("MilkevsMultiBlockLibrary::MultiBlockEntity::getRotatedStructureMatrixList - invalid direction passed through!");
            return getStructureMatrixList();
        }
        Block[][][][] rotatedStructureMatrixList = getStructureMatrixList();
        for(int y = 0; y < rotatedStructureMatrixList.length; y++) {
            Block[][][] layer = rotatedStructureMatrixList[y];
            //rotate
            for(int r = 0; r < rotations; r++) {
                //System.out.println("Input: " + Arrays.deepToString(layer));
                //transpose
                for(int i = 0; i < layer.length; i++) {
                    for(int j = i + 1; j < layer.length; j++) {
                        Block[] temp = layer[i][j];
                        layer[i][j] = layer[j][i];
                        layer[j][i] = temp;
                    }
                }
                //System.out.println("Transpose: " + Arrays.deepToString(layer));
                //reverse rows
                for(int i = 0; i < layer.length; i++) {
                    int start = 0;
                    int end = layer.length - 1;
                    while(start < end) {
                        Block[] temp = layer[i][start];
                        layer[i][start] = layer[i][end];
                        layer[i][end] = temp;
                        start++;
                        end--;
                    }
                }
                //System.out.println("Reverse Rows: " + Arrays.deepToString(layer));
            }
            rotatedStructureMatrixList[y] = layer;
        }
        //System.out.println(rotatedStructureMatrixPure.length);
        //System.out.println(Arrays.deepToString(getStructureMatrixPure()[1]));
        //System.out.println(Arrays.deepToString(rotatedStructureMatrixPure[1]));
        return rotatedStructureMatrixList;
    }
    
    //find the position of the controller (its named stupidly i know, good thing yall dont need to use it)
    private BlockPos getStructureOffsetList(Block[][][][] structureMatrix) {
        for (int y = 0; y < structureMatrix.length; y++) {
            Block[][][] yMatrix = structureMatrix[y];
            for (int x = 0; x < yMatrix.length; x++) {
                Block[][] xMatrix = yMatrix[x];
                for (int z = 0; z < xMatrix.length; z++) {
                    Block[] block = xMatrix[z];
                    if(block[0] == getWorld().getBlockState(this.pos).getBlock()) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return new BlockPos(0,0,0);
    }
    
    //used to sync data client needs for preview render
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        updateClient();
        return super.toUpdatePacket();
    }
    
    //the only data we need to save is the preview and direction. i would also love to save this.blocksMissing but idfk how to translate that to nbt. wish they implemented synced components earlier
    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("preview", this.renderPreview);
        nbt.putInt("direction", this.direction.getId());
        nbt.putBoolean("valid", this.valid);
        super.writeNbt(nbt, registries);
    }
 
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if(nbt.contains("preview")) this.renderPreview = nbt.getBoolean("preview");
        if(nbt.contains("direction")) this.direction = Direction.byId(nbt.getInt("direction"));
        if(nbt.contains("valid")) this.valid = nbt.getBoolean("valid");
        updateClient();
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        updateClient();
        return createNbt(registryLookup);
    }
    
    //used to sync data client needs for preview render
    //this caches the payload (which will be updated everytime this is called) and the payload will be sent on the next tick of this block entity
    public void updateClient() {
        if(this.world != null) {
            if (!this.world.isClient()) {
                //System.out.println("RenderPreview: " + this.renderPreview);
                //System.out.println("Blocks Missing: " + this.blocksMissing);
                //System.out.println("Pos: " + this.pos);
                //System.out.println("Direction: " + this.direction);
                //System.out.println("updating payload!");
                payload = new MultiBlockEntityComponent.MultiBlockEntityPayload(this.renderPreview, this.blocksMissing, this.pos, this.direction);
            }
        }
    }
    
    //used to sync data client needs for preview render
    public void applyPayload(MultiBlockEntityComponent.MultiBlockEntityPayload payload) {
        if(this.world.isClient()) {
            this.renderPreview = payload.render();
            this.blocksMissing = payload.missingBlocks();
            this.direction = payload.direction();
            markDirty();
        }
    }
     
    //use this to check if your structure is valid
    public boolean isStructureValid() {
        return this.valid;
    }
    
    //this controls the orientation the builder attempts to build in. may or may not line up with the actual "direction" of the multiblock, serves purely as a control
    public void setDirection(Direction dir) {
        direction = dir;
    }
    
    //this controls the orientation the builder attempts to build in. may or may not line up with the actual "direction" of the multiblock, serves purely as a control
    public Direction getDirection() {
        return direction;
    }
    
    //this returns a map of all blocks in the structure. you can search this list for important blocks you need references to such as io blocks
    //the blockPos here is global
    public Map<BlockPos, Block> getBlocksInStructure() {
        return blocksInStructure;
    }
    
    //this returns a matrix of blocks that are missing in the structure, its just used by the builder to determine what next to place
    public Map<BlockPos, List<Block>> getBlocksMissing() {
        return isStructureValid() ? null : blocksMissing;
    }
    
    //used to determine if the client should render the preview
    public boolean isRenderPreview() {
        return renderPreview && !isStructureValid();
    }

    //this is a matrix containing your structure made up of direct block references!
    //if a portion of your structure has to air, use Blocks.STRUCTURE_VOID for those spots. Blocks.AIR can be any block
    //WARNING: structure files MUST BE SQUARE HORIZONTALLY. Vertically as well if you want your structure to rotate vertically (vertical rotation not implemented yet) 
    //if you dont want your actual structure to be square, fill in the blank areas with Blocks.AIR
    //list accepts any block in the array at each location. i would recommend making arrays of blocks that are possible at a location and inputting those arrays into the matrix, for readability of your code/structure matrix
    protected abstract Block[][][][] getStructureMatrixList();
    //Example implementation:
    /*
        @Override
    protected Block[][][][] getStructureMatrixList() {
        Block[] walls = new Block[]{Blocks.NETHERITE_BLOCK,Blocks.ANCIENT_DEBRIS};
        Block[] tips = new Block[]{Blocks.DIAMOND_BLOCK,Blocks.LAPIS_BLOCK};
        Block[] core = new Block[]{MilkevsMultiBlockLibrary.EXAMPLE_MULTIBLOCK};
        Block[] air = new Block[]{Blocks.AIR};
        Block[] sVoid = new Block[]{Blocks.STRUCTURE_VOID};
        Block[] chest = new Block[]{Blocks.CHEST};
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
     */
    //if the structure can be built facing up/down. not implemented yet.
    protected abstract boolean structureCanRotateVertically();
    
    
}
