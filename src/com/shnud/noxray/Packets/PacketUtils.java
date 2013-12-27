package com.shnud.noxray.Packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.ArrayList;

/**
 * Created by Andrew on 27/12/2013.
 */
public class PacketUtils {

    public static ArrayList<PacketType> getAllEntityUpdatePackets() {
        ArrayList<PacketType> types = new ArrayList<PacketType>();
        types.add(PacketType.Play.Server.ANIMATION);
        types.add(PacketType.Play.Server.ATTACH_ENTITY);
        types.add(PacketType.Play.Server.COLLECT);
        types.add(PacketType.Play.Server.BED);
        types.add(PacketType.Play.Server.ENTITY);
        types.add(PacketType.Play.Server.ENTITY_EFFECT);
        types.add(PacketType.Play.Server.ENTITY_EQUIPMENT);
        types.add(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        types.add(PacketType.Play.Server.ENTITY_LOOK);
        types.add(PacketType.Play.Server.ENTITY_METADATA);
        types.add(PacketType.Play.Server.ENTITY_MOVE_LOOK);
        types.add(PacketType.Play.Server.ENTITY_STATUS);
        types.add(PacketType.Play.Server.ENTITY_TELEPORT);
        types.add(PacketType.Play.Server.ENTITY_VELOCITY);
        types.add(PacketType.Play.Server.REL_ENTITY_MOVE);
        types.add(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        return types;
    }

    public static int getEntityIDFromEntityPacket(PacketContainer packet) {
        PacketType type = packet.getType();

        if(type == PacketType.Play.Server.ATTACH_ENTITY || type == PacketType.Play.Server.COLLECT)
            return packet.getIntegers().read(1);
        else
            return packet.getIntegers().read(0);
    }

    public static ArrayList<PacketType> getAllBlockUpdatePackets() {
        ArrayList<PacketType> types = new ArrayList<PacketType>();
        types.add(PacketType.Play.Server.BLOCK_ACTION);
        types.add(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        types.add(PacketType.Play.Server.BLOCK_CHANGE);
        types.add(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        types.add(PacketType.Play.Server.EXPLOSION);
        types.add(PacketType.Play.Server.TILE_ENTITY_DATA);
        types.add(PacketType.Play.Server.UPDATE_SIGN);
        return types;
    }

    public static ArrayList<PacketType> getAllEntitySpawnPackets() {
        ArrayList<PacketType> types = new ArrayList<PacketType>();
        types.add(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB);
        types.add(PacketType.Play.Server.SPAWN_ENTITY);
        types.add(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        types.add(PacketType.Play.Server.SPAWN_ENTITY_PAINTING);
        return types;
    }
}
