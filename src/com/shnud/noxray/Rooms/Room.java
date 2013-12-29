package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.XY;

import java.util.ArrayList;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {

    public static final Room notARoom = new Room(0, null, 0);
    public static final int NOT_A_ROOM_ID = 0;

    private int _id;
    private ArrayList<XY> _knownChunks;
    private long _timeCreated;

    public Room(int id, ArrayList<XY> knownChunks, long timeCreated) {
        if(id < 0)
            throw new IllegalArgumentException("Room id must be greater than 0");

        _id = id;
        if(knownChunks == null)
            knownChunks = new ArrayList<XY>();
        else
            _knownChunks = knownChunks;

        _timeCreated = timeCreated;
    }

    public int getId() {
        return _id;
    }

    public ArrayList<XY> getListOfKnownChunks() {
        return _knownChunks;
    }
}
