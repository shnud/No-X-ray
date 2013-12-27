package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.Coords2D;

import java.util.ArrayList;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Room {

    private int _id;
    private ArrayList<Coords2D> _knownChunks;
    private long _timeCreated;

    public Room(int id, ArrayList<Coords2D> knownChunks, long timeCreated) {
        _id = id;
        _knownChunks = knownChunks;
        _timeCreated = timeCreated;
    }
}
