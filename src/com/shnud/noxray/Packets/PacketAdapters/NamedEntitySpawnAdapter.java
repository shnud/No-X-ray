package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.PlayerSpawnPacketEvent;
import org.bukkit.entity.Player;

public class NamedEntitySpawnAdapter extends NoXrayPacketAdapter {

    public NamedEntitySpawnAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, PacketType.Play.Server.NAMED_ENTITY_SPAWN);
    }

    @Override
    protected void packetSendingImplementation(final PacketEvent event) {
        dispatchEventToListeners(
                new PlayerSpawnPacketEvent(
                        event,
                        (Player) new WrapperPlayServerNamedEntitySpawn(event.getPacket())
                                .getEntity(event.getPlayer().getWorld())
                )
        );
    }
}
