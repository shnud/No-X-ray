package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;

public abstract class EntityDestroyPacketEvent extends NoXrayPacketEvent {
    public EntityDestroyPacketEvent(PacketEvent event) {
        super(event);
    }

    public abstract Entity getEntity();
}
