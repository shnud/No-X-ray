package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunkBulk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.MapChunkPacketEvent;
import com.shnud.noxray.Packets.PacketHelpers.AbstractMapChunkPacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.MapChunkBulkPacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.MapChunkPacketHelper;

public class MapChunkAdapter extends NoXrayPacketAdapter {

    public MapChunkAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK);
    }

    @Override
    public void packetSendingImplementation(final PacketEvent event) {
        if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
            dispatchEventToListeners(new MapChunkPacketEvent(event) {
                @Override
                public AbstractMapChunkPacketHelper getMapChunkPacketHelper() {
                    return new MapChunkPacketHelper(new WrapperPlayServerMapChunk(event.getPacket()));
                }
            });
        }
        else if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
            dispatchEventToListeners(new MapChunkPacketEvent(event) {
                @Override
                public AbstractMapChunkPacketHelper getMapChunkPacketHelper() {
                    return new MapChunkBulkPacketHelper(new WrapperPlayServerMapChunkBulk(event.getPacket()));
                }
            });
        }
    }
}
