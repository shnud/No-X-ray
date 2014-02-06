package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.PacketHelpers.AbstractMapChunkPacketHelper;

public abstract class MapChunkPacketEvent extends NoXrayPacketEvent {
    public MapChunkPacketEvent(PacketEvent event) {
        super(event);
    }

    public abstract AbstractMapChunkPacketHelper getMapChunkPacketHelper();
}
