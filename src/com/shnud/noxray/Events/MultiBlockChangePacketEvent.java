package com.shnud.noxray.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Utilities.MagicValues;
import com.shnud.noxray.World.MapBlock;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper for a multi block packet change event that allows read/write/removal of blocks
 * We have already created code to write multi block packets in the MultiBlockChangePacketSender so we probably need to just
 * create pure wrappers for packets like multi-block change packets and then use that to read and write to packets
 * as well as create new ones. For now, this will do.
 */
public class MultiBlockChangePacketEvent extends BlockChangePacketEvent {
    private final int _chunkX;
    private final int _chunkZ;
    private static final int BYTES_PER_BLOCK_CHANGE_RECORD = 4;

    public MultiBlockChangePacketEvent(Player receiver, PacketEvent event) {
        super(receiver, event);
        if(event.getPacketType() != PacketType.Play.Server.MULTI_BLOCK_CHANGE)
            throw new IllegalArgumentException("Packet type must be MULTI_BLOCK_CHANGE");

        _chunkX = getPacket().getChunkCoordIntPairs().read(0).getChunkX();
        _chunkZ = getPacket().getChunkCoordIntPairs().read(0).getChunkZ();
    }

    private byte[] getBlockDataFromPacket() {
        return getPacket().getByteArrays().read(0);
    }

    @Override
    public int getNumberOfBlocks() {
        return getPacket().getIntegers().read(0);
    }

    @Override
    public MapBlock getBlock(int i) {
        if(i > getNumberOfBlocks() - 1)
            throw new ArrayIndexOutOfBoundsException(i);

        int byteArrayIndex = i * BYTES_PER_BLOCK_CHANGE_RECORD;
        byte[] data = getBlockDataFromPacket();

        // F0 00 00 00 <- x
        int x = (data[byteArrayIndex + 0] >> 4) & 0x0F;
        x += _chunkX * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK;
        // 0F 00 00 00 <- z
        int z = data[byteArrayIndex + 0] & 0x0F;
        z += _chunkZ * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK;
        // 00 FF 00 00 <- y
        int y = data[byteArrayIndex + 1] & 0xFF;
        // 00 00 FF F0 <- block ID
        int blockID = 0;
        blockID += (data[byteArrayIndex + 2] << 4) & 0xFF0;
        blockID += (data[byteArrayIndex + 3] >> 4) & 0x00F;
        // 00 00 00 0F <- block metadata
        byte metadata = (byte) (data[byteArrayIndex + 3] & 0x0F);

        return new MapBlock(blockID, metadata, x, y, z);
    }

    /**
     * Set the index to any given block. The blocks coordinates must be within the chunks bounds, as multi block
     * packets are tied to one chunk only. This is not checked by the method and therefore any chunks which are
     * outside the chunk boundaries will be treated as if they were in this chunk and will get converted to chunk
     * relative coordinates
     * @param i the index to set the block at
     * @param block the block to change for the receiver (coordinates in block/global coordinates, e.g. 1066, -661 is acceptable)
     */
    @Override
    public void setBlock(int i, MapBlock block) {
        if(i > getNumberOfBlocks() - 1)
            throw new ArrayIndexOutOfBoundsException(i);

        int byteArrayIndex = i * BYTES_PER_BLOCK_CHANGE_RECORD;
        byte[] data = getBlockDataFromPacket();

        // This uses the same code as the packetsender for multi block changes
        // Should really use some central packet wrapper for stuff like this

        // F0 00 00 00 <- x
        data[byteArrayIndex + 0] |= (block.getX() % MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK) << 4;
        // 0F 00 00 00 <- z
        data[byteArrayIndex + 0] |= (block.getZ() % MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK);
        // 00 FF 00 00 <- y
        data[byteArrayIndex + 1] |= block.getY();
        // 00 00 FF F0 <- block ID
        data[byteArrayIndex + 2] |= block.getBlockID() >> 4;
        data[byteArrayIndex + 3] |= block.getBlockID() << 4;
        // 00 00 00 0F <- block metadata
        data[byteArrayIndex + 3] |= block.getMetadata();
    }

    @Override
    public void setBlocks(List<MapBlock> blocks) {
        if(blocks.size() > getNumberOfBlocks()) {
            getPacket().getByteArrays().write(0, new byte[blocks.size() * BYTES_PER_BLOCK_CHANGE_RECORD]);
        }

        for(int i = 0; i < blocks.size(); i++) {
            setBlock(i, blocks.get(i));
        }
    }

    @Override
    public LinkedList<MapBlock> getBlocks() {
        LinkedList<MapBlock> blocks = new LinkedList<MapBlock>();

        for(int i = 0; i < getNumberOfBlocks(); i++) {
            blocks.add(getBlock(i));
        }

        return blocks;
    }

    public int getChunkX() {
        return _chunkX;
    }

    public int getChunkZ() {
        return _chunkZ;
    }
}
