package net.milkev.milkevsmultiblocklibrary.common.blockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.Arrays;
import java.util.Objects;

public abstract class MultiBlockEntity extends BlockEntity {

    private boolean valid;
    private BlockPos controllerBlockPos;
    
    public MultiBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
    
    public boolean validateStructure() {
        if(!Objects.requireNonNull(getWorld()).isClient()) {
            if (getStructureMatrixPure().length > 0) {
                for (int i = 0; i < 4; i++) {
                    Block[][][] rotatedStructureMatrixPure = getRotatedStructureMatrixPure(i);
                    controllerBlockPos = getStructureOffset(rotatedStructureMatrixPure);
                    //System.out.println("Structure Offset is: " + controllerBlockPos);
                    if (validateStructurePure(rotatedStructureMatrixPure)) {
                        valid = true;
                        return true;
                    }
                }
            } else if(getStructureMatrixList().length > 0) {
                for (int i = 0; i < 4; i++) {
                    Block[][][][] rotatedStructureMatrixList = getRotatedStructureMatrixList(i);
                    if (validateStructureList(rotatedStructureMatrixList)) {
                        valid = true;
                        return true;
                    }
                }
            }
        }
        valid = false;
        return false;
    }

    private boolean validateStructurePure(Block[][][] structureMatrix) {
        boolean check = true;
        for (int y = 0; y < structureMatrix.length; y++) {
            Block[][] yMatrix = structureMatrix[y];
            for (int x = 0; x < yMatrix.length; x++) {
                Block[] xMatrix = yMatrix[x];
                for (int z = 0; z < xMatrix.length; z++) {
                    Block block = xMatrix[z];
                    BlockPos checkLocation = new BlockPos(x, y ,z);
                    if(!checkBlock(checkLocation, block)) {
                        check = false;
                    }
                }
            }
        }
        if(check) {
            //System.out.println("Structure Valid!");
        } else {
            //System.out.println("Structure Invalid!");
        }
        return check;
    }
    
    private boolean validateStructureList(Block[][][][] structureMatrixList) {
        boolean check = true;
        for (int y = 0; y < structureMatrixList.length; y++) {
            Block[][][] yMatrix = structureMatrixList[y];
            for (int x = 0; x < yMatrix.length; x++) {
                Block[][] xMatrix = yMatrix[x];
                for (int z = 0; z < xMatrix.length; z++) {
                    Block[] blocks = xMatrix[z];
                    BlockPos checkLocation = new BlockPos(x, y ,z);
                    if(!checkBlocks(checkLocation, blocks)) {
                        check = false;
                    }
                }
            }
        }
        if(check) {
            //System.out.println("Structure Valid!");
        } else {
            //System.out.println("Structure Invalid!");
        }
        return check;
    }

    private BlockPos getLocalBlockPos(BlockPos blockPos) {
        //System.out.println("Controller Block Pos:" + controllerBlockPos + " blockPos:" + blockPos + " this.pos: " + this.pos);
        return this.pos.add(blockPos).subtract(controllerBlockPos);
    }
    
    private boolean checkBlock(BlockPos blockPos, Block block) {
        //System.out.println("Found block: " + getWorld().getBlockState(getLocalBlockPos(blockPos)).getBlock() + " at: " + getLocalBlockPos(blockPos) + " and wanted block: " + block);
        if(block == Blocks.AIR) {
            return true;
        }
        if(block == Blocks.STRUCTURE_VOID) {
            return getWorld().getBlockState(getLocalBlockPos(blockPos)).getBlock() == Blocks.AIR;
        }
        if(getWorld().getBlockState(getLocalBlockPos(blockPos)).getBlock() == block) {
            return true;
        }
        return false;
    }
    
    private boolean checkBlocks(BlockPos blockPos, Block[] blocks) {
        for(int i = 0; i < blocks.length; i++) {
            if(checkBlock(blockPos, blocks[i])) {
                return true;
            }
        }
        return false;
    }
    
    private Block[][][] getRotatedStructureMatrixPure(int x) {
        int rotations = x%4;
        Block[][][] rotatedStructureMatrixPure = getStructureMatrixPure();
        for(int y = 0; y < getStructureMatrixPure().length; y++) {
            Block[][] layer = rotatedStructureMatrixPure[y];
            //rotate
            for(int r = 0; r < rotations; r++) {
                //System.out.println("Input: " + Arrays.deepToString(layer));
                //transpose
                for(int i = 0; i < layer.length; i++) {
                    for(int j = i + 1; j < layer.length; j++) {
                        Block temp = layer[i][j];
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
                        Block temp = layer[i][start];
                        layer[i][start] = layer[i][end];
                        layer[i][end] = temp;
                        start++;
                        end--;
                    }
                }
                //System.out.println("Reverse Rows: " + Arrays.deepToString(layer));
            }
            rotatedStructureMatrixPure[y] = layer;
        }
        //System.out.println(rotatedStructureMatrixPure.length);
        //System.out.println(Arrays.deepToString(getStructureMatrixPure()[1]));
        //System.out.println(Arrays.deepToString(rotatedStructureMatrixPure[1]));
        return rotatedStructureMatrixPure;
    }
    
    private Block[][][][] getRotatedStructureMatrixList(int i) {
        return getStructureMatrixList();
    }
    
    private BlockPos getStructureOffset(Block[][][] structureMatrix) {
        if(getStructureMatrixPure().length > 0) {
            for (int y = 0; y < structureMatrix.length; y++) {
                Block[][] yMatrix = structureMatrix[y];
                for (int x = 0; x < yMatrix.length; x++) {
                    Block[] xMatrix = yMatrix[x];
                    for (int z = 0; z < xMatrix.length; z++) {
                        Block block = xMatrix[z];
                        if(block == getWorld().getBlockState(this.pos).getBlock()) {
                            return new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }
        return new BlockPos(0,0,0);
    }
    
    public boolean isStructureValid() {
        return this.valid;
    }
    
    //this is a matrix containing your structure made up of direct block references!
    //if a portion of your structure has to air, use Blocks.STRUCTURE_VOID for those spots. Blocks.AIR can be any block
    //WARNING: structure files MUST BE SQUARE HORIZONTALLY. Vertically as well if you want your structure to rotate vertically. if you dont want your actual structure to be square, fill in the blank areas with Blocks.AIR
    //pure only accepts one particular block at each location
    protected abstract Block[][][] getStructureMatrixPure();
    //list accepts any block in the array at each location. i would recommend making arrays of blocks that are possible at a location and inputting those arrays into the matrix, for readability of your code/structure matrix
    protected abstract Block[][][][] getStructureMatrixList();
    //if the structure can be built facing up/down
    protected abstract boolean structureCanRotateVertically();
    
}
