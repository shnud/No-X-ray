package com.shnud.noxray.Events;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 04/01/2014.
 */
public class PlayerDestroyPacketEvent extends BaseEntityPacketEvent {

    public PlayerDestroyPacketEvent(Player receiver, Entity subject, PacketEvent event) {
        super(receiver, subject, event);
    }

    /*
     * For this, because the packet is an aggregate of other entities being destroyed as well,
     * don't cancel the whole event, but search through the packet for this entity and remove it
     */
    public void cancel() {
        PacketContainer packet = getPacketEvent().getPacket();
        int[] entityIDs = packet.getIntegerArrays().read(0);
        for(int entity : entityIDs) {
            if(entity == getSubject().getEntityId())
                entity = 0;
        }
        packet.getIntegerArrays().write(0, entityIDs);
    }
}
