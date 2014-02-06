package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.WrapperPlayServerAttachEntity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.EntityUpdatePacketEvent;
import org.bukkit.entity.Entity;

public class MultipleEntityUpdateAdapter extends NoXrayPacketAdapter {

    public MultipleEntityUpdateAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, PacketType.Play.Server.ATTACH_ENTITY);
    }

    @Override
    protected void packetSendingImplementation(final PacketEvent event) {
        // Dispatch a separate event for both the vehicle and the entity attached to it

        dispatchEventToListeners(new EntityUpdatePacketEvent(event) {
            @Override
            public Entity getEntity() {
                return new WrapperPlayServerAttachEntity(event.getPacket()).getEntity(event);
            }
        });

        dispatchEventToListeners(new EntityUpdatePacketEvent(event) {
            @Override
            public Entity getEntity() {
                return new WrapperPlayServerAttachEntity(event.getPacket()).getVehicle(event);
            }
        });
    }
}
