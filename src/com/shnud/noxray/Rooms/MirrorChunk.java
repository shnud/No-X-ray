package com.shnud.noxray.Rooms;

import com.shnud.noxray.Structures.BooleanArray;
import com.shnud.noxray.Structures.NibbleArray;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.World;

import java.util.ArrayList;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorChunk {

    private static final int NOT_A_ROOM_KEY = 0;
    private static final int NOT_A_ROOM_ID = Room.NOT_A_ROOM_ID;
    private static final int MAX_UNIQUE_KEYS_PER_CHUNK = 15;
    private NibbleArray _data;
    private int _x, _z;
    private int[] _keyToId;
    private World _world;
    private boolean _hasChangedSinceInit = false;

    public static MirrorChunk constructFromExistingData(World world, int x, int z, NibbleArray data, int[] keys) {
        return new MirrorChunk(world, x, z, data, keys);
    }

    public static MirrorChunk constructBlankMirrorChunk(World world, int x, int z) {
        return new MirrorChunk(world, x, z);
    }

    private MirrorChunk(World world, int x, int z, NibbleArray data, int[] keys) {
        if(keys.length != MAX_UNIQUE_KEYS_PER_CHUNK)
            throw new IllegalArgumentException("Keys array must be " + MAX_UNIQUE_KEYS_PER_CHUNK + " long max");

        if(data.getLength() != MagicValues.BLOCKS_IN_CHUNK)
            throw new IllegalArgumentException("Invalid length of array, " + MagicValues.BLOCKS_IN_CHUNK + " blocks required");

        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _x = x;
        _z = z;
        _data = data;
        _keyToId = keys;
    }

    private MirrorChunk(World world, int x, int z) {
        this(world, x, z, new NibbleArray(MagicValues.BLOCKS_IN_CHUNK), new int[MAX_UNIQUE_KEYS_PER_CHUNK]);
    }

    private int addNewId(int id) throws MirrorChunkKeysFullException {
        int existing = keyIndexForID(id);
        if(existing >= 0)
            return existing;

        int unused = firstUnusedKeySlot();
        if(firstUnusedKeySlot() < 0)
            throw new MirrorChunkKeysFullException();

        _keyToId[unused] = id;
        _hasChangedSinceInit = true;
        return unused;
    }

    private int firstUnusedKeySlot() {
        for(int i = 0; i < _keyToId.length; i++) {
            if(_keyToId[i] == NOT_A_ROOM_ID)
                return i;
        }

        return -1;
    }

    private int indexOfLocalBlock(int x, int y, int z) {
        return (y * 256) + (z * 16) + x;
    }

    private int keyIndexForID(int id) {
        for(int i = 0; i < _keyToId.length; i++) {
            if(_keyToId[i] == id)
                return i;
        }

        return -1;
    }

    private void setIdAtLocalBlock(int x, int y, int z, int id) throws MirrorChunkKeysFullException {
        if(x < 0 || x > 15 || z < 0 || z > 15 || y < 0 || y > 255)
            throw new IllegalArgumentException("Coordinates not within chunk bounds");

        int key;

        if(id == NOT_A_ROOM_ID)
            key = NOT_A_ROOM_KEY;
        else {
            key = keyIndexForID(id);

            // Key doesn't exist, try to add it
            if(key < 0)
                key = addNewId(id);
            // Will throw an exception if the key cannot be added
        }

        // See getID() function for explanation as to why we are
        // adding 1 to the key before adding it to the chunk data

        int index = indexOfLocalBlock(x, y, z);
        _data.setValueAtIndex(index, (byte) (key + 1));

        _hasChangedSinceInit = true;
    }

    public boolean containsKeyForID(int id) {
        return keyIndexForID(id) >= 0;
    }

    public boolean isFull() {
        return firstUnusedKeySlot() < 0;
    }

    public int getIdAtLocalBlock(int x, int y, int z) {
        if(x < 0 || x > 15 || z < 0 || z > 15 || y < 0 || y > 255)
            throw new IllegalArgumentException("Coordinates not within chunk bounds");

        int index = indexOfLocalBlock(x, y, z);
        int key = _data.getValueAtIndex(index);

        /*
         * When retreiving keys from the chunk data, we have to negate
         * 1 from the result, as 0 is used to mean unobfuscated/not a room.
         * Because of this, values 1 - 15 inclusive are valid, however the
         * key->id array is only 15 long, and arrays are zero indexed,
         * so 1 must be become 0 etc. The opposite is applied when
         * writing keys to the chunk data.
         */

        if(key == NOT_A_ROOM_KEY)
            return NOT_A_ROOM_ID;
        else
            return _keyToId[key - 1];
    }

    public int getX() {
        return _x;
    }

    public int getZ() {
        return _z;
    }

    public void removeId(int id) {
        int index = keyIndexForID(id);

        if(id < 0)
            return;

        for (int i = 0; i < _data.getLength(); i++) {
            if(_data.getValueAtIndex(i) == id)
                _data.setValueAtIndex(i, (byte)15);
        }

        _keyToId[index] = 0;
        _hasChangedSinceInit = true;
    }

    public class MirrorChunkKeysFullException extends Exception {}

    /*
     * Searches through the whole of the chunk data and checks
     * for any keys and ids which aren't being utilised. Returns a specific
     * class so as to make use of the results of the clean up.
     */
    public MirrorChunkCleanUpResults cleanUp() {
        MirrorChunkCleanUpResults results = new MirrorChunkCleanUpResults();

        /*
         * BooleanArray of all of the key slots that are found to be in use
         * by the data. If the key slot was not used, and it turns out there
         * is a key->id pair in the key array, then it will zeroed so that it
         * can be used for a new room if necessary.
         */
        BooleanArray foundKeys = new BooleanArray(NOT_A_ROOM_KEY);

        for (int i = 0; i < _data.getLength(); i++) {
            byte key = (byte) _data.getValueAtIndex(i);

            if(key != NOT_A_ROOM_KEY) {

                /*
                 * Here the if statement checks whether a key found in the chunk data
                 * actually relates to an id in the key->id array. If it doesn't, then it's
                 * useless and is possibly making compression off the chunk less efficient
                 */

                if(_keyToId[key] == Room.NOT_A_ROOM_ID) {
                    _data.setValueAtIndex(i, (byte) NOT_A_ROOM_KEY);
                    results.cleanedKeys++;
                }

                foundKeys.setValueAtIndex(_data.getValueAtIndex(i), true);
            }
        }

        for (int i = 0; i < foundKeys.getLength(); i++) {
            if(foundKeys.getValueAtIndex(i) == false) {
                results.addCleanedID(_keyToId[i]);
                _keyToId[i] = NOT_A_ROOM_KEY;
            }
        }

        if(results.getCleanedKeysAmount() > 0 || results.getCleanedIDs().length > 0)
            _hasChangedSinceInit = true;

        return results;
    }

    private class MirrorChunkCleanUpResults {
        private int cleanedKeys = 0;
        private ArrayList<Integer> _cleanedIDs = new ArrayList<Integer>();

        public void addCleanedID(int id) {
            _cleanedIDs.add(id);
        }

        public int getCleanedKeysAmount() {
            return cleanedKeys;
        }

        public Integer[] getCleanedIDs() {
            return (Integer[]) _cleanedIDs.toArray();
        }
    }
}
