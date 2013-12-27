package com.shnud.noxray.Events;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 26/12/2013.
 */
public class EntitySpawnPacketEvent extends BaseEntityPacketEvent {

    public EntitySpawnPacketEvent(Player receiver, Entity subject, PacketEvent event) {
        super(receiver, subject, event);
    }
}
