package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.DynamicCoordinates;

import java.io.RandomAccessFile;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorChunk {

    private static final boolean SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL = true;
    private MirrorChunkData _data;
    private MirrorChunkIDMap _keyToIDMap;
    private int _x, _z;
    private MirrorChunkEventListener _listener;
    private long _timeOfLastCleanUp = 0;

    public MirrorChunk(int x, int z) {
        _x = x;
        _z = z;
        _data = MirrorChunkData.createBlank();
        _keyToIDMap = new MirrorChunkIDMap();
    }

    public void saveToFileAtOffset(RandomAccessFile ram, long filePointer) {

    }

    public void loadFromFileAtOffset(RandomAccessFile ram, long filePointer) {

    }

    public void setBlockAsPartOfRoom(DynamicCoordinates coordinates, int roomID) {

    }

    public boolean isEmpty() {
        /*
         * Both of these are useless without each other, so if either one is empty,
         * we can report that the chunk is empty.
         */
        return _data.isEmpty() || _keyToIDMap.isEmpty();
    }

    public void setListener(MirrorChunkEventListener listener) {
        _listener = listener;
    }
}
