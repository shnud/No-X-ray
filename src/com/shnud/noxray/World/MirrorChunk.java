package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.DynamicCoordinates;

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
    private MirrorChunkEventListener _listener;

    public MirrorChunk(int x, int z) {
        _x = x;
        _z = z;
        _data = MirrorChunkKeyData.createBlank();
        _keyToIDMap = new MirrorChunkIDMap();
    }

    public void saveToFileAtOffset(RandomAccessFile ram, long filePointer) {

    }

    public void loadFromFileAtOffset(RandomAccessFile ram, long filePointer) {

    }

    public void setBlockToRoomID(DynamicCoordinates coordinates, int roomID) throws MirrorChunkIDMap.ChunkIDSlotsFullException {
        if(roomID < 0)
            throw new IllegalArgumentException("Room ID must be 0 (not a room) or greater");

        int oldKey = _data.getBlockKey(coordinates);

        int key;

        if(roomID != 0 && !_keyToIDMap.containsRoomID(roomID)) {
            key = _keyToIDMap.addRoomID(roomID);

            if(_listener != null)
                _listener.roomAddedToChunkEvent(roomID, _x, _z);
        }
        else
            key = _keyToIDMap.getKeyForRoomID(roomID);

        if(key == oldKey)
            return;

        _data.setBlockKey(coordinates, key);
        if(_listener != null)
            _listener.chunkChangeEvent(_x, _z);
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

            if(_listener != null)
                _listener.roomRemovedFromChunkEvent(roomID, _x, _z);
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

    public void setListener(MirrorChunkEventListener listener) {
        _listener = listener;
    }

    public static class MirrorChunkFullException extends Exception {}
}
