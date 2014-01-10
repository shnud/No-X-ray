package com.shnud.noxray.World;

/**
 * Created by Andrew on 09/01/2014.
 */
public class Block {

    private final byte _metadata;
    private final int _blockID;
    private final int _x, _y, _z;

    public Block(int blockID, int x, int y, int z) {
        this(blockID, (byte) 0, x, y, z);
    }

    public Block(int blockID, byte metadata, int x, int y, int z) {
        _blockID = blockID;
        _metadata = metadata;
        _x = x;
        _y = y;
        _z = z;
    }
}
