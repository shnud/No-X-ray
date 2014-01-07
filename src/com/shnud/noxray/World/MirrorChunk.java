package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.DynamicCoordinates;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorChunk {

    private static final boolean SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL = true;
    private MirrorChunkKeyData _data;
    private MirrorChunkIDMap _keyToIDMap;
    private int _x, _z;
    private long _timeOfLastCleanUp = 0;

    public MirrorChunk(int x, int z) {
        _x = x;
        _z = z;
        _data = MirrorChunkKeyData.createBlank();
        _keyToIDMap = new MirrorChunkIDMap();
    }

    public void saveToFile(RandomAccessFile ram) throws IOException {
        ram.writeLong(_timeOfLastCleanUp);

        int[] slots = _keyToIDMap.getSlots();
        ram.writeByte(slots.length - 1);

        for(int slot = 1; slot < slots.length; slot++) {
            ram.writeInt(slots[slot]);
        }

        _data.writeToFile(ram);
    }

    public void loadFromFile(RandomAccessFile ram) throws IOException {
        _timeOfLastCleanUp = ram.readLong();

        int keySlotAmount = ram.readByte();

        for(int i = 1; i < keySlotAmount + 1; i++) {
            int id = ram.readInt();
            _keyToIDMap.setSlotToID(i, id);
        }

        _data.readFromFile(ram);
    }

    public boolean setBlockToRoomID(DynamicCoordinates coordinates, int roomID) {
        if(roomID < 0)
            throw new IllegalArgumentException("Room ID must be 0 (not a room) or greater");

        int key;

        if(roomID != 0 && !_keyToIDMap.containsRoomID(roomID)) {
            key = _keyToIDMap.addRoomID(roomID);
            if(key < 0)
                return false;
        }
        else
            key = _keyToIDMap.getKeyForRoomID(roomID);

        _data.setBlockKey(coordinates, key);
        return true;
    }

    public int getRoomIDAtBlock(DynamicCoordinates coordinates) {
        int key = _data.getBlockKey(coordinates);

        return _keyToIDMap.getRoomIDForKey(key);
    }

    public void removeRoomID(int roomID) {
        int key = _keyToIDMap.getKeyForRoomID(roomID);

        if(key > 0) {
            _data.removeAllKeys(key);
            _keyToIDMap.removeRoomID(roomID);
        }
    }

    public boolean isEmpty() {
        /*
         * Both of these are useless without each other, so if either one is empty,
         * we can report that the chunk is empty.
         */
        return _data.isEmpty() || _keyToIDMap.isEmpty();
    }

    public boolean isFull() {
        return _keyToIDMap.isFull();
    }

    public boolean containsRoomID(int roomID) {
        return _keyToIDMap.containsRoomID(roomID);
    }

    public void cleanUp() {
        _timeOfLastCleanUp = System.currentTimeMillis();
    }

    public boolean isSectionEmpty(int section) {
        if(section < 0 || section > 15)
            throw new IllegalArgumentException("Section must be between 0 and 15 inclusive");

        return _data.isSectionEmpty(section);
    }
}
