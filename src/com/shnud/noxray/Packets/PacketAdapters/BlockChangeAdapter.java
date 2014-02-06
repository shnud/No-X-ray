package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.BlockChangePacketEvent;
import com.shnud.noxray.Packets.PacketHelpers.AbstractBlockChangePacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.MultiBlockChangePacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.SingleBlockChangePacketHelper;

public class BlockChangeAdapter extends NoXrayPacketAdapter {
    public BlockChangeAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Server.MULTI_BLOCK_CHANGE);
    }

    @Override
    public void packetSendingImplementation(final PacketEvent event) {
        if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            dispatchEventToListeners(new BlockChangePacketEvent(event) {
                @Override
                public AbstractBlockChangePacketHelper getBlockChangePacketHelper() {
                    return new MultiBlockChangePacketHelper(
                            new WrapperPlayServerMultiBlockChange(event.getPacket())
                    );
                }
            });
        }
        else if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            dispatchEventToListeners(new BlockChangePacketEvent(event) {
                @Override
                public AbstractBlockChangePacketHelper getBlockChangePacketHelper() {
                    return new SingleBlockChangePacketHelper(
                            new WrapperPlayServerBlockChange(event.getPacket())
                    );
                }
            });
        }
    }
}
