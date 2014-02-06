package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.PacketHelpers.AbstractBlockChangePacketHelper;

public abstract class BlockChangePacketEvent extends NoXrayPacketEvent {
    public BlockChangePacketEvent(PacketEvent event) {
        super(event);
    }

    public abstract AbstractBlockChangePacketHelper getBlockChangePacketHelper();
}
