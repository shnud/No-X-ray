package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableMap;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.EntityUpdatePacketEvent;
import org.bukkit.entity.Entity;

import java.util.Map;

public class SingleEntityUpdateAdapter extends NoXrayPacketAdapter {

    private static interface EntityFromPacketEvent {
        public Entity get(PacketEvent event);
    }

    private static final Map<PacketType, EntityFromPacketEvent> _packetTypes
            = new ImmutableMap.Builder<PacketType, EntityFromPacketEvent>()
            .put(PacketType.Play.Server.ENTITY_EFFECT,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityEffect(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_EQUIPMENT,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityEquipment(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_HEAD_ROTATION,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityHeadRotation(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_LOOK,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityLook(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_MOVE_LOOK,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityMoveLook(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.REL_ENTITY_MOVE,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerRelEntityMove(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_METADATA,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityMetadata(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_STATUS,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityStatus(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_TELEPORT,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityTeleport(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.ENTITY_VELOCITY,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerEntityVelocity(event.getPacket()).getEntity(event);
                        }
                    }
            ).put(PacketType.Play.Server.REMOVE_ENTITY_EFFECT,
                    new EntityFromPacketEvent() {
                        @Override
                        public Entity get(PacketEvent event) {
                            return new WrapperPlayServerRemoveEntityEffect(event.getPacket()).getEntity(event);
                        }
                    }
            ).build();

    public SingleEntityUpdateAdapter(Iterable<IPacketEventWrapperListener> listeners) {
        super(listeners, (PacketType[]) _packetTypes.keySet().toArray());
    }

    @Override
    public void packetSendingImplementation(final PacketEvent event) {
        dispatchEventToListeners(new EntityUpdatePacketEvent(event) {
            @Override
            public Entity getEntity() {
                return _packetTypes.get(event.getPacketType()).get(event);
            }
        });
    }
}
