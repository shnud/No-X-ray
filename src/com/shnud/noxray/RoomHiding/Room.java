package com.shnud.noxray.RoomHiding;

import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.XZ;

import java.util.HashSet;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {
    public static final int NOT_A_ROOM_ID = 0;
    public static final Room NOT_A_ROOM = new Room(NOT_A_ROOM_ID);
    private final int _ID;
    private final HashSet<XZ> _knownChunks = new HashSet<XZ>();

    public Room(int ID) {
        if(ID < 0)
            throw new IllegalArgumentException("Room id must be greater than or equal to 0");

        _ID = ID;
    }

    public int getID() {
        return _ID;
    }

    public HashSet<XZ> getKnownChunks() {
        return _knownChunks;
    }

    public void addChunk(XZ coords) {
        if(!_knownChunks.contains(coords))
            _knownChunks.add(coords);
    }

    public void removeChunk(XZ coords) {
        if(_knownChunks.contains(coords))
            _knownChunks.remove(coords);
    }

    public boolean isInChunk(XZ coords) {
        return _knownChunks.contains(coords);
    }

    public boolean isInChunk(DynamicCoordinates coords) {
        if(!coords.isPreciseEnoughFor(DynamicCoordinates.PrecisionLevel.CHUNK))
            throw new IllegalArgumentException("Coordinates are not at the chunk precision level");

        return isInChunk(new XZ(coords.chunkX(), coords.chunkZ()));
    }

    public boolean equals(Object o) {
        if(o instanceof Room) {
            if(((Room) o).getID() == _ID)
                return true;
            else
                return false;
        }
        else
            return false;
    }

    public boolean isValidRoom() {
        return !NOT_A_ROOM.equals(this);
    }
}
