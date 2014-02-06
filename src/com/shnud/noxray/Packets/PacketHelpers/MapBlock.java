package com.shnud.noxray.Packets.PacketHelpers;

import org.bukkit.Material;

import javax.annotation.concurrent.Immutable;

/**
 * Created by Andrew on 09/01/2014.
 */
@Immutable
public class MapBlock {

    private final byte _metadata;
    private final int _blockID;
    private final int _x, _y, _z;

    public MapBlock(int blockID, int x, int y, int z) {
        this(blockID, (byte) 0, x, y, z);
    }

    public MapBlock(int blockID, byte metadata, int x, int y, int z) {
        _blockID = blockID;
        _metadata = metadata;
        _x = x;
        _y = y;
        _z = z;
    }

    public int getX() {
        return _x;
    }

    public int getY() {
        return _y;
    }

    public int getZ() {
        return _z;
    }

    public byte getMetadata() {
        return _metadata;
    }

    public int getBlockID() {
        return _blockID;
    }

    public String toString() {
        return "[" + _x + ", " + _y + ", " + _z + "] blockType: " + Material.getMaterial(_blockID);
    }
}
