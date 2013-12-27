package com.shnud.noxray.Rooms;

import com.shnud.noxray.Structures.NibbleArray;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorChunk {

    private static final int MAXIMUM_KEYS_PER_CHUNK = 15;
    private NibbleArray _data;
    private int _x, _z;
    private int[] _keyToId;
    private int _retainCount;

    public void retain() {
        _retainCount++;
    }

    public void release() {
        _retainCount--;
    }

    public int retainCount() {
        return _retainCount;
    }

    public static MirrorChunk constructFromExistingData(int x, int z, NibbleArray data, int[] keys) {
        return new MirrorChunk(x, z, data, keys);
    }

    public static MirrorChunk constructBlankMirrorChunk(int x, int z) {
        return new MirrorChunk(x, z);
    }

    private MirrorChunk(int x, int z, NibbleArray data, int[] keys) {
        if(keys.length != MAXIMUM_KEYS_PER_CHUNK)
            throw new IllegalArgumentException("Keys must be 15 long, 15 unique keys for each chunk");

        if(data.getLength() != 65536)
            throw new IllegalArgumentException("Invalid length of array, 65536 blocks required");

        _x = x;
        _z = z;
        _data = data;
        _keyToId = keys;
    }

    private MirrorChunk(int x, int z) {
        this(x, z, new NibbleArray(65536), new int[MAXIMUM_KEYS_PER_CHUNK]);
    }

    private int addNewId(int id) throws MirrorChunkKeysFullException {
        int existing = keyIndexOfId(id);
        if(existing >= 0)
            return existing;

        int unused = firstUnusedKeySlot();
        if(firstUnusedKeySlot() < 0)
            throw new MirrorChunkKeysFullException();

        _keyToId[unused] = id;
        return unused;
    }

    private int firstUnusedKeySlot() {
        for(int i = 0; i < _keyToId.length; i++) {
            if(_keyToId[i] == 0)
                return i;
        }

        return -1;
    }

    private int indexOfLocalBlock(int x, int y, int z) {
        return (y * 256) + (z * 16) + (x * 16);
    }

    private int keyIndexOfId(int id) {
        for(int i = 0; i < _keyToId.length; i++) {
            if(_keyToId[i] == id)
                return i;
        }

        return -1;
    }

    public boolean containsId(int id) {
        return keyIndexOfId(id) >= 0;
    }

    public boolean isFull() {
        return firstUnusedKeySlot() < 0;
    }

    public void setIdAtLocalBlock(int x, int y, int z, int id) throws MirrorChunkKeysFullException {
        if(x < 0 || x > 15 || z < 0 || z > 15 || y < 0 || y > 255)
            throw new IllegalArgumentException("Coordinates not within chunk bounds");

        int key = keyIndexOfId(id);

        // Key doesn't exist, try to add it
        if(key < 0)
            key = addNewId(id);
            // Will throw an exception if the key cannot be added

        int index = indexOfLocalBlock(x, y, z);
        _data.setValueAtIndex(index, (byte) key);
    }

    public int getIdAtLocalBlock(int x, int y, int z) {
        if(x < 0 || x > 15 || z < 0 || z > 15 || y < 0 || y > 255)
            throw new IllegalArgumentException("Coordinates not within chunk bounds");

        int index = indexOfLocalBlock(x, y, z);
        int key = _data.getValueAtIndex(index);

        if(key == 15)
            return 0;
        else
            return _keyToId[key];
    }

    public NibbleArray getData() {
        return _data;
    }

    public int getX() {
        return _x;
    }

    public int getZ() {
        return _z;
    }

    public int[] getKeyArray() {
        return _keyToId;
    }

    public class MirrorChunkKeysFullException extends Exception {}

    public void removeId(int id) {
        int index = keyIndexOfId(id);

        if(id < 0)
            return;

        for (int i = 0; i < _data.getLength(); i++) {
            if(_data.getValueAtIndex(i) == id)
                _data.setValueAtIndex(i, (byte)15);
        }

        _keyToId[index] = 0;
    }
}
