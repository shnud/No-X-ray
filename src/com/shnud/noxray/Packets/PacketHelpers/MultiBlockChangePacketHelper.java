package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.BlockChangeArray;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.shnud.noxray.Utilities.MagicValues;
import com.shnud.noxray.Utilities.MathHelper;

import java.util.LinkedList;
import java.util.List;

public class MultiBlockChangePacketHelper extends AbstractBlockChangePacketHelper {

    public MultiBlockChangePacketHelper(WrapperPlayServerMultiBlockChange packet) {
        super(packet);
    }

    protected WrapperPlayServerMultiBlockChange getWrappedPacket() {
        return ((WrapperPlayServerMultiBlockChange) super.getWrappedPacket());
    }

    @Override
    protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
        return new WrapperPlayServerMultiBlockChange(packet);
    }

    @Override
    protected PacketType getAllowedPacketType() {
        return PacketType.Play.Server.MULTI_BLOCK_CHANGE;
    }

    @Override
    public int getAmountOfBlockChanges() {
        return getWrappedPacket().getRecordCount();
    }

    @Override
    public MapBlock getBlockChange(int i) {
        BlockChangeArray.BlockChange change = getWrappedPacket().getRecordDataArray().getBlockChange(i);

        return new MapBlock(
                change.getBlockID(),
                (byte) change.getMetadata(),
                change.getRelativeX() + (getWrappedPacket().getChunkX() * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK),
                change.getAbsoluteY(),
                change.getRelativeZ() + (getWrappedPacket().getChunkZ() * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK)
        );
    }

    @Override
    public void setBlockChange(int i, MapBlock block) {
        BlockChangeArray changes = getWrappedPacket().getRecordDataArray();
        addMapBlockToBlockChangeArray(i, block, changes);
    }

    @Override
    public void setBlockChanges(List<MapBlock> blocks) {
        // Create a new block change array here because we might have
        // a different amount of block changes to begin with
        BlockChangeArray changes = new BlockChangeArray(blocks.size());

        for(int i = 0; i < blocks.size(); i++) {
            addMapBlockToBlockChangeArray(i, blocks.get(i), changes);
        }

        getWrappedPacket().setRecordData(changes);
        // Update the record count incase we have a different number
        // of block changes than the packet originally had
        getWrappedPacket().setRecordCount((short) changes.getSize());
    }

    @Override
    public LinkedList<MapBlock> getBlockChanges() {
        LinkedList<MapBlock> changes = new LinkedList<MapBlock>();

        for(int i = 0; i < getAmountOfBlockChanges(); i++) {
            changes.add(getBlockChange(i));
        }

        return changes;
    }

    private void addMapBlockToBlockChangeArray(int index, MapBlock block, BlockChangeArray array) {
        BlockChangeArray.BlockChange change = array.getBlockChange(index);

        if(!isWithinChunkBounds(block))
            throw new IllegalArgumentException("Block was not within chunk bounds");

        change.setBlockID(block.getBlockID());
        change.setMetadata(block.getMetadata());
        change.setRelativeX(MathHelper.positiveMod(block.getX(),  MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK));
        change.setRelativeZ(MathHelper.positiveMod(block.getZ(), MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK));
        change.setAbsoluteY(block.getY());
    }

    private boolean isWithinChunkBounds(MapBlock block) {
        return block.getX() >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK == getWrappedPacket().getChunkX() &&
                block.getZ() >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK == getWrappedPacket().getChunkZ();
    }
}
