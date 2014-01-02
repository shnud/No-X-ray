package com.shnud.noxray.World;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Created by Andrew on 31/12/2013.
 */
public class MirrorChunkIDMap {

    private static final int MAXIMUM_SLOTS = 63;
    /**
     * Holds all of the room IDs that are stored in a chunk, which are accesed by index or 'key' stored in the chunk data.
     * Slot 0 is reserved for room 0, i.e. not a room, in which case the room hider will not try to hide that block.
     */
    private ArrayList<Integer> _slots = Lists.newArrayList((Integer) 0);

    public boolean isEmpty() {
        for(int ID : _slots) {
            if(ID != 0)
                return false;
        }

        return true;
    }

    public int getRoomIDForKey(int key) {
        return _slots.get(key);
    }

    public int getKeyForRoomID(int roomID) {
        return _slots.indexOf(roomID);
    }

    public void setSlotToID(int slot, int roomID) {
        if(slot == 0)
            throw new IllegalArgumentException("Cannot add a room ID to slot 0 as it is reserved for 'not a room' status");

        if(slot >= _slots.size())
            _slots.ensureCapacity(slot + 1);

        _slots.set(slot, roomID);
    }

    public int getAmountOfSlots() {
        /*
         * Here we take away 1 because 0 is never used as a proper room ID slot and is reserved for "not a room", i.e. 0
         */
        return _slots.size() - 1;
    }

    public boolean containsRoomID(int roomID) {
        return getKeyForRoomID(roomID) != 0;
    }

    public int addRoomID(int roomID) throws ChunkIDSlotsFullException {
        int slot = _slots.indexOf(roomID);

        if(slot > 0)
            return slot;

        int firstEmpty = -1;

        for(int i = 1; i < _slots.size(); i++) {
            if (_slots.get(i) == 0)
                firstEmpty = i;
        }

        if(firstEmpty > 0) {
            _slots.set(firstEmpty, roomID);
            return firstEmpty;
        }
        else {
            if(_slots.size() < MAXIMUM_SLOTS) {
                _slots.add(roomID);
                return _slots.size() - 1;
            }
            else
                throw new ChunkIDSlotsFullException();
        }
    }

    public class ChunkIDSlotsFullException extends Exception {}
}
