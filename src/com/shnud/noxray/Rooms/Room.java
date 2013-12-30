package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.XY;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {

    public static final Room notARoom = new Room(0);
    public static final int NOT_A_ROOM_ID = 0;

    private int _id;
    private ArrayList<XY> _knownChunks = new ArrayList<XY>();

    public Room(int id) {
        if(id < 0)
            throw new IllegalArgumentException("Room id must be greater than 0");

        _id = id;
    }

    public int getID() {
        return _id;
    }

    public ArrayList<XY> getListOfKnownChunks() {
        return _knownChunks;
    }

    public void addChunk(XY coords) {
        _knownChunks.add(coords);
    }

    public void removeChunk(XY coords) {
        Iterator it = _knownChunks.iterator();

        while(it.hasNext()) {
            XY chunk = (XY) it.next();

            if(chunk.x == coords.x && chunk.z == coords.z) {
                it.remove();
                return;
            }
        }
    }
}
