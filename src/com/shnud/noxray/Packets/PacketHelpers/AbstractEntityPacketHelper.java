package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Packets.PacketEvents.NoXrayPacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public abstract class AbstractEntityPacketHelper extends AbstractPacketHelper {

    public AbstractEntityPacketHelper(AbstractPacket packet) {
        super(packet);
    }

    public AbstractEntityPacketHelper(PacketContainer packet) {
        super(packet);
    }

    public abstract Entity getEntity(World world);

    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    public Entity getEntity(NoXrayPacketEvent event) {
        return getEntity(event.getReceiver().getWorld());
    }

    public class EntitySpawnPacketHelper extends AbstractEntityPacketHelper {

        public EntitySpawnPacketHelper(WrapperPlayServerEntity packet) {
            super(packet);
        }

        public EntitySpawnPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntity) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntity(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY;
        }
    }

    public class EntityMoveLookPacketHelper extends AbstractEntityPacketHelper {

        public EntityMoveLookPacketHelper(WrapperPlayServerEntityMoveLook packet) {
            super(packet);
        }

        public EntityMoveLookPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityMoveLook) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityMoveLook(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_MOVE_LOOK;
        }
    }

    public class EntityRelativeMovePacketHelper extends AbstractEntityPacketHelper {

        public EntityRelativeMovePacketHelper(WrapperPlayServerRelEntityMove packet) {
            super(packet);
        }

        public EntityRelativeMovePacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerRelEntityMove) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerRelEntityMove(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.REL_ENTITY_MOVE;
        }
    }

    public class EntityLookPacketHelper extends AbstractEntityPacketHelper {

        public EntityLookPacketHelper(WrapperPlayServerEntityLook packet) {
            super(packet);
        }

        public EntityLookPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityLook) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityLook(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_LOOK;
        }
    }

    public class EntityMetadataPacketHelper extends AbstractEntityPacketHelper {

        public EntityMetadataPacketHelper(WrapperPlayServerEntityMetadata packet) {
            super(packet);
        }

        public EntityMetadataPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityMetadata) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityMetadata(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_METADATA;
        }
    }

    public class EntityEffectPacketHelper extends AbstractEntityPacketHelper {

        public EntityEffectPacketHelper(WrapperPlayServerEntityEffect packet) {
            super(packet);
        }

        public EntityEffectPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityEffect) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityEffect(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_EFFECT;
        }
    }

    public class EntityEquipmentPacketHelper extends AbstractEntityPacketHelper {

        public EntityEquipmentPacketHelper(WrapperPlayServerEntityEquipment packet) {
            super(packet);
        }

        public EntityEquipmentPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityEquipment) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityEquipment(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_EQUIPMENT;
        }
    }

    public class EntityTeleportPacketHelper extends AbstractEntityPacketHelper {

        public EntityTeleportPacketHelper(WrapperPlayServerEntityTeleport packet) {
            super(packet);
        }

        public EntityTeleportPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityTeleport) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityTeleport(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_TELEPORT;
        }
    }

    public class EntityVelocityPacketHelper extends AbstractEntityPacketHelper {

        public EntityVelocityPacketHelper(WrapperPlayServerEntityVelocity packet) {
            super(packet);
        }

        public EntityVelocityPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityVelocity) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityVelocity(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_VELOCITY;
        }
    }

    public class EntityHeadRotationPacketHelper extends AbstractEntityPacketHelper {

        public EntityHeadRotationPacketHelper(WrapperPlayServerEntityHeadRotation packet) {
            super(packet);
        }

        public EntityHeadRotationPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityHeadRotation) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityHeadRotation(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_HEAD_ROTATION;
        }
    }

    public class EntityStatusPacketHelper extends AbstractEntityPacketHelper {

        public EntityStatusPacketHelper(WrapperPlayServerEntityStatus packet) {
            super(packet);
        }

        public EntityStatusPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerEntityStatus) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerEntityStatus(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.ENTITY_STATUS;
        }
    }

    public class EntityRemoveEffectPacketHelper extends AbstractEntityPacketHelper {

        public EntityRemoveEffectPacketHelper(WrapperPlayServerRemoveEntityEffect packet) {
            super(packet);
        }

        public EntityRemoveEffectPacketHelper(PacketContainer packet) {
            super(packet);
        }

        @Override
        public Entity getEntity(World world) {
            return ((WrapperPlayServerRemoveEntityEffect) getWrappedPacket()).getEntity(world);
        }

        @Override
        protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
            return new WrapperPlayServerRemoveEntityEffect(packet);
        }

        @Override
        protected PacketType getAllowedPacketType() {
            return PacketType.Play.Server.REMOVE_ENTITY_EFFECT;
        }
    }
}
