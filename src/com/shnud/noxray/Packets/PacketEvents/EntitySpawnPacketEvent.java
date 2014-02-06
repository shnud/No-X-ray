package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;

public abstract class EntitySpawnPacketEvent extends NoXrayPacketEvent {

    public EntitySpawnPacketEvent(PacketEvent event) {
        super(event);
    }

    public abstract Entity getEntity();
}
