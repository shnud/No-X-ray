package com.shnud.noxray.Packets.PacketWrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

/**
 * Created by Andrew on 07/01/2014.
 */
public class BlockChangePacketWrapper extends AbstractPacketWrapper {

    public BlockChangePacketWrapper(PacketContainer packet) {
        super(packet);
    }

    @Override
    protected PacketType getPacketType() {
        return PacketType.Play.Server.BLOCK_CHANGE;
    }

    public int getX() {
        return getPacket().getIntegers().read(0);
    }

    public int getY() {
        return getPacket().getIntegers().read(1);
    }

    public int getZ() {
        return getPacket().getIntegers().read(2);
    }


}
