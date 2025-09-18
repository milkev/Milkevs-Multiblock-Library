package net.milkev.milkevsmultiblocklibrary.common.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.milkev.milkevsmultiblocklibrary.client.render.MultiBlockEntityRenderer;
import net.milkev.milkevsmultiblocklibrary.common.MilkevsMultiBlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public final class MultiBlockEntityComponent {
    
    public static final Codec<MultiBlockEntityComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.BOOL.fieldOf("controllerblockpos").forGetter(MultiBlockEntityComponent::isRender),
                Codec.unboundedMap(BlockPos.CODEC, Codec.list(Block.CODEC.codec())).fieldOf("missingblocks").forGetter(MultiBlockEntityComponent::getMap),
                Direction.CODEC.fieldOf("direction").forGetter(MultiBlockEntityComponent::getDirection)
        ).apply(i, MultiBlockEntityComponent::new));;
    
    private final boolean render;
    private final Map<BlockPos, List<Block>> map;
    private final Direction direction;

    public MultiBlockEntityComponent(boolean render, Map<BlockPos, List<Block>> map, Direction direction) {
        this.render = render;
        this.map = map;
        this.direction = direction;
    }
    
    public MultiBlockEntityComponent() {
        this.render = true;
        this.map = new LinkedHashMap<>();
        this.direction = Direction.NORTH;
    }

    public boolean isRender() {
        return render;
    }

    public Map<BlockPos, List<Block>> getMap() {
        return map;
    }

    public Direction getDirection() {
        return direction;
    }

    public record MultiBlockEntityPayload(boolean render, Map<BlockPos, List<Block>> missingBlocks, BlockPos blockEntityPos, Direction direction) implements CustomPayload {

        public static final CustomPayload.Id<MultiBlockEntityPayload> ID = new CustomPayload.Id<>(Identifier.of(MilkevsMultiBlockLibrary.MOD_ID, "multiblock_entity_payload"));
        
        public static final PacketCodec<RegistryByteBuf, MultiBlockEntityPayload> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, 
                    MultiBlockEntityPayload::render,
                PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, PacketCodecs.registryValue(RegistryKeys.BLOCK).collect(PacketCodecs.toList())), 
                    MultiBlockEntityPayload::missingBlocks,
                BlockPos.PACKET_CODEC,
                    MultiBlockEntityPayload::blockEntityPos,
                Direction.PACKET_CODEC,
                    MultiBlockEntityPayload::direction,
                MultiBlockEntityPayload::new
        );
        

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
