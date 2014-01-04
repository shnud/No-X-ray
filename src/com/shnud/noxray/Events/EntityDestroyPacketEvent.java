package com.shnud.noxray.Events;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 04/01/2014.
 */
public class EntityDestroyPacketEvent extends PlayerDestroyPacketEvent {

    public EntityDestroyPacketEvent(Player receiver, Entity subject, PacketEvent event) {
        super(receiver, subject, event);
    }
}
