package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableMap;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.EntitySpawnPacketEvent;
import org.bukkit.entity.Entity;

import java.util.Map;

public class EntitySpawnAdapter extends NoXrayPacketAdapter {
    private static interface EntityFromPacketEvent {
        public Entity get(PacketEvent event);
    }

    private static final Map<PacketType, EntityFromPacketEvent> _packetTypes
            = new ImmutableMap.Builder<PacketType, EntityFromPacketEvent>()
            .put(PacketType.Play.Server.ENTITY,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntity(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.SPAWN_ENTITY,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerSpawnEntity(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerSpawnEntityExperienceOrb(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.SPAWN_ENTITY_LIVING,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerSpawnEntityLiving(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.SPAWN_ENTITY_PAINTING,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerSpawnEntityPainting(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.SPAWN_ENTITY_WEATHER,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return ProtocolLibrary.getProtocolManager().getEntityFromID(
                                    event.getPlayer().getWorld(),
                                    new WrapperPlayServerSpawnEntityWeather(event.getPacket()).getEntityId()
                            );
                        }
                    }
            ).build();

    public EntitySpawnAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, (PacketType[]) _packetTypes.keySet().toArray());
    }

    @Override
    public void packetSendingImplementation(final PacketEvent event) {
        dispatchEventToListeners(new EntitySpawnPacketEvent(event) {
            @Override
            public Entity getEntity() {
                return _packetTypes.get(event.getPacketType()).get(event);
            }
        });
    }
}
