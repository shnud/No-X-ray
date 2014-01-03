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
        ram.writeByte(_keyToIDMap.getAmountOfSlots());

        for(int key = 1; key < _keyToIDMap.getAmountOfSlots(); key++) {
            ram.writeInt(_keyToIDMap.getRoomIDForKey(key));
        }

        _data.writeToFile(ram);
    }

    public void loadFromFile(RandomAccessFile ram) throws IOException {
        _timeOfLastCleanUp = ram.readLong();
        int keySlotAmount = ram.readByte();

        for(int i = 0; i < keySlotAmount; i++) {
            _keyToIDMap.setSlotToID(i + 1, ram.readInt());
        }

        _data.readFromFile(ram);
    }

    public void setBlockToRoomID(DynamicCoordinates coordinates, int roomID) throws MirrorChunkIDMap.ChunkIDSlotsFullException {
        if(roomID < 0)
            throw new IllegalArgumentException("Room ID must be 0 (not a room) or greater");

        int oldKey = _data.getBlockKey(coordinates);

        int key;

        if(roomID != 0 && !_keyToIDMap.containsRoomID(roomID))
            key = _keyToIDMap.addRoomID(roomID);
        else
            key = _keyToIDMap.getKeyForRoomID(roomID);

        if(key == oldKey)
            return;

        _data.setBlockKey(coordinates, key);
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

    public static class MirrorChunkFullException extends Exception {}
}
