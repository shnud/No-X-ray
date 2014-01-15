package com.shnud.noxray.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.World.MapBlock;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A packet event which is used to wrap around block change packets to allow reading/writing/cancelling of individual blocks
 */
public abstract class BlockChangePacketEvent extends BasePacketEvent {

    public BlockChangePacketEvent(Player receiver, PacketEvent event) {
        super(receiver, event);
    }

    public abstract int getNumberOfBlocks();
    public abstract MapBlock getBlock(int i);
    public abstract void setBlock(int i, MapBlock block);

    /**
     * Set the blocks in the packet change event to those specified in the given list
     * @param blocks the block changes to send
     */
    public abstract void setBlocks(List<MapBlock> blocks);

    /**
     * Returns a copy as a list of the blocks contained in this block change packet event
     * @return a list (copy) of the block changes
     */
    public abstract LinkedList<MapBlock> getBlocks();
}
