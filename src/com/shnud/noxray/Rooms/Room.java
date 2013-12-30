package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.XZ;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {

    public static final Room notARoom = new Room(0);
    public static final int NOT_A_ROOM_ID = 0;

    private int _id;
    private ArrayList<XZ> _knownChunks = new ArrayList<XZ>();

    public Room(int id) {
        if(id < 0)
            throw new IllegalArgumentException("Room id must be greater than 0");

        _id = id;
    }

    public int getID() {
        return _id;
    }

    public ArrayList<XZ> getListOfKnownChunks() {
        return _knownChunks;
    }

    public void addChunk(XZ coords) {
        _knownChunks.add(coords);
    }

    public void removeChunk(XZ coords) {
        Iterator it = _knownChunks.iterator();

        while(it.hasNext()) {
            XZ chunk = (XZ) it.next();

            if(chunk.x == coords.x && chunk.z == coords.z) {
                it.remove();
                return;
            }
        }
    }
}
