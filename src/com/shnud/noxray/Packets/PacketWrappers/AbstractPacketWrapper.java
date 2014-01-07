package com.shnud.noxray.Packets.PacketWrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

/**
 * Created by Andrew on 07/01/2014.
 */
public abstract class AbstractPacketWrapper {

    private final PacketContainer _packet;

    public AbstractPacketWrapper(final PacketContainer packet) {
        if(packet.getType() != getPacketType())
            throw new IllegalArgumentException("Wrong packet type - found: " + packet.getType() + " required: " + getPacketType());

        _packet = packet;
    }

    protected final PacketContainer getPacket() {
        return _packet;
    }

    protected abstract PacketType getPacketType();
}
