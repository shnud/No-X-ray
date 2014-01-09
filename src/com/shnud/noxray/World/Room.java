package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.XZ;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {

    public static final Room notARoom = new Room(0);
    public static final int NOT_A_ROOM_ID = 0;

    private int _id;
    private HashSet<XZ> _knownChunks = new HashSet<XZ>();

    public Room(int id) {
        if(id < 0)
            throw new IllegalArgumentException("Room id must be greater than 0");

        _id = id;
    }

    public int getID() {
        return _id;
    }

    public HashSet<XZ> getKnownChunks() {
        return _knownChunks;
    }

    public void addChunk(XZ coords) {
        if(!_knownChunks.contains(coords))
            _knownChunks.add(coords);
    }

    public void removeChunk(XZ coords) {
        _knownChunks.remove(coords);
    }

    public boolean isInChunk(XZ coords) {
        return _knownChunks.contains(coords);
    }
}
