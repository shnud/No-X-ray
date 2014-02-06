package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.EntityDestroyPacketEvent;
import org.bukkit.entity.Entity;

import java.util.List;

public class EntityDestroyAdapter extends NoXrayPacketAdapter {

    public EntityDestroyAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, PacketType.Play.Server.ENTITY_DESTROY);
    }

    @Override
    public void packetSendingImplementation(final PacketEvent event) {
        final WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy(event.getPacket());

        for(final int subjectID : packet.getEntities()) {
            dispatchEventToListeners(
                    new EntityDestroyPacketEvent(event) {
                        @Override
                        public void cancel() {
                            List<Integer> entities = packet.getEntities();
                            entities.remove(subjectID);

                            if(!entities.isEmpty())
                                packet.setEntities(entities);
                            else
                                super.cancel();
                        }

                        @Override
                        public Entity getEntity() {
                            return ProtocolLibrary.getProtocolManager().getEntityFromID(event.getPlayer().getWorld(),
                                    subjectID);
                        }
                    }
            );
        }
    }
}
