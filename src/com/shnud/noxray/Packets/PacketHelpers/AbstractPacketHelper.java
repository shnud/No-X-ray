package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public abstract class AbstractPacketHelper {

    private final AbstractPacket _packet;

    public AbstractPacketHelper(AbstractPacket packet) {
        _packet = packet;
    }

    public AbstractPacketHelper(PacketContainer packet) {
        if(packet.getType() != getAllowedPacketType())
            throw new IllegalArgumentException("Incorrect packet type for this helper");

        _packet = getWrappedPacketFromPacket(packet);
    }

    protected AbstractPacket getWrappedPacket() {
        return _packet;
    }

    protected abstract AbstractPacket getWrappedPacketFromPacket(PacketContainer packet);

    protected abstract PacketType getAllowedPacketType();
}
