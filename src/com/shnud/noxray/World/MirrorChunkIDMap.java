package com.shnud.noxray.World;

/**
 * Created by Andrew on 31/12/2013.
 */
public class MirrorChunkIDMap {

    private static final int MAXIMUM_SLOTS = 63;
    /**
     * Holds all of the room IDs that are stored in a chunk, which are accesed by index or 'key' stored in the chunk data.
     * Slot 0 is reserved for room 0, i.e. not a room, in which case the room hider will not try to hide that block.
     */
    private int[] _slots = new int[MAXIMUM_SLOTS + 1];

    /**
     * Return the first empty ID slot (ignores 0 as it's always empty)
     * @return the first empty slot, -1 if no empty slot was found
     */
    private int firstEmpty() {
        for(int i = 1; i < _slots.length; i++) {
            if(_slots[i] == 0)
                return i;
        }

        return -1;
    }

    public boolean isEmpty() {
        for(int ID : _slots) {
            if(ID != 0)
                return false;
        }

        return true;
    }

    public boolean isFull() {
        if(_slots.length < MAXIMUM_SLOTS + 1)
            return false;

        if(firstEmpty() < 0)
            return true;
        else
            return false;
    }

    public int getRoomIDForKey(int key) {
        return _slots[key];
    }

    public int indexOf(int roomID) {
        for(int i = 0; i < _slots.length; i++) {
            if(_slots[i] == roomID)
                return i;
        }

        return -1;
    }

    public int getKeyForRoomID(int roomID) {
        return indexOf(roomID);
    }

    public void setSlotToID(int slot, int roomID) {
        if(slot == 0)
            throw new IllegalArgumentException("Cannot add a room ID to slot 0 as it is reserved for 'not a room' status");

        _slots[slot] = roomID;
    }

    public int[] getSlots() {
        return _slots;
    }

    public boolean containsRoomID(int roomID) {
        return getKeyForRoomID(roomID) > 0;
    }

    public int addRoomID(int roomID) {
        int slot = indexOf(roomID);

        if(slot > 0)
            return slot;

        int firstEmpty = firstEmpty();

        if(firstEmpty > 0) {
            _slots[firstEmpty] = roomID;
            return firstEmpty;
        }
        else
            return -1;
    }

    public void removeRoomID(int roomID) {
        if(roomID == 0)
            return;

        for(int i = 1; i < _slots.length; i++) {
            if(_slots[i] == roomID)
                _slots[i] = 0;
        }
    }

    public class ChunkIDSlotsFullException extends Exception {}
}
