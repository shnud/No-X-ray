package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;

import java.util.LinkedList;
import java.util.List;

/**
 * A packet event which is used to wrap around block change packets to allow reading/writing/cancelling of individual blocks
 */
public abstract class AbstractBlockChangePacketHelper extends AbstractPacketHelper {

    public AbstractBlockChangePacketHelper(AbstractPacket packet) {
        super(packet);
    }

    public abstract int getAmountOfBlockChanges();
    public abstract MapBlock getBlockChange(int i);
    public abstract void setBlockChange(int i, MapBlock block);

    /**
     * Set the blocks in the packet change event to those specified in the given list
     * @param blocks the block changes to send
     */
    public abstract void setBlockChanges(List<MapBlock> blocks);

    /**
     * Returns a copy as a list of the blocks contained in this block change packet event
     * @return a list (copy) of the block changes
     */
    public abstract LinkedList<MapBlock> getBlockChanges();
}
