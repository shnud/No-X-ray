package com.shnud.noxray.Rooms;

import com.shnud.noxray.Structures.BooleanArray;
import org.bukkit.World;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorChunk {

    private static final int NOT_A_ROOM_KEY = 0;
    private static final int NOT_A_ROOM_ID = Room.NOT_A_ROOM_ID;
    private static final int MAX_UNIQUE_KEYS_PER_CHUNK = 15;
    private static final boolean SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL = true;
    private SplitChunkData _data;
    private int _x, _z;
    private int[] _keyToId;
    private boolean _hasChangedSinceInit = false;

    private MirrorChunk(int x, int z, SplitChunkData data, int[] keys) {
        if(keys.length != MAX_UNIQUE_KEYS_PER_CHUNK)
            throw new IllegalArgumentException("Keys array must be " + MAX_UNIQUE_KEYS_PER_CHUNK + " long max");

        if(data == null)
            throw new IllegalArgumentException("Chunk data cannot be null");

        _x = x;
        _z = z;
        _data = data;
        _keyToId = keys;
    }

    public static MirrorChunk constructFromFileAtOffset(int x, int z, RandomAccessFile file, long fileOffset) throws IOException, DataFormatException {
        file.seek(fileOffset);

        int keys[] = new int[MAX_UNIQUE_KEYS_PER_CHUNK];
        for(int i = 0; i < MAX_UNIQUE_KEYS_PER_CHUNK; i++) keys[i] = file.readInt();

        SplitChunkData data = SplitChunkData.createFromFileAtOffset(file, file.getFilePointer());

        return new MirrorChunk(x, z, data, keys);
    }

    public void saveToFileAtOffset(RandomAccessFile file, long fileOffset) throws IOException {
        file.seek(fileOffset);

        for(int key : _keyToId) {
            file.writeInt(key);
        }

        _data.writeToFileAtOffset(file, file.getFilePointer());
    }

    public static MirrorChunk constructBlankMirrorChunk(int x, int z) {
        return new MirrorChunk(x, z, SplitChunkData.createBlank(), new int[MAX_UNIQUE_KEYS_PER_CHUNK]);
    }

    private int addNewRoomIDToKeys(int id) throws MirrorChunkKeysFullException {
        int existing = keyIndexForID(id);
        if(existing >= 0)
            return existing;

        int unused = firstUnusedKeySlot();
        if(unused < 0) {
            if(!SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL || cleanUp().getCleanedKeysAmount() < 1)
                throw new MirrorChunkKeysFullException();
            else {
                unused = firstUnusedKeySlot();
                /*
                 * Possibly notify the Rooms of the removed ids from the key->id array
                 * as they still think that there is part of that room stored in this chunk
                 */
            }
        }

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

    private void setRoomIDAtLocalBlock(int x, int y, int z, int roomID) throws MirrorChunkKeysFullException {
        if(x < 0 || x > 15 || z < 0 || z > 15 || y < 0 || y > 255)
            throw new IllegalArgumentException("Coordinates not within chunk bounds");

        int key;

        if(roomID == NOT_A_ROOM_ID)
            key = NOT_A_ROOM_KEY;
        else {
            key = keyIndexForID(roomID);

            /*
             * If an existing key did not already exist, check to see if it's possible
             * to add one. If it's not, throw an exception. Otherwise, continue to add it.
             */
            if(isFull() && key < 0)
                throw new MirrorChunkKeysFullException();
            else
                key = addNewRoomIDToKeys(roomID);
        }

        /*
         * See getID() function for an explanation as to why we are
         * adding 1 to the key before adding it to the chunk data
         */
        int index = indexOfLocalBlock(x, y, z);
        int oldKey = _data.getValueAtIndex(index);
        _data.setValueAtIndex(index, (byte) (key + 1));

        if(oldKey != key)
            _hasChangedSinceInit = true;
    }

    public boolean containsKeyForRoomID(int roomId) {
        return keyIndexForID(roomId) >= 0;
    }

    public boolean isFull() {
        if(firstUnusedKeySlot() < 0 && SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL)
            cleanUp();

        return firstUnusedKeySlot() < 0;
    }

    /**
     * Get the roomID at any given coordinates, coordinates must be specified relative to chunk
     *
     * @param x chunk-relative x coordinate
     * @param y chunk-relative y coordinate
     * @param z chunk-relative z coordinate
     * @return the room ID at the given coordinates, returns NOT_A_ROOM_ID if no ID is found
     */
    public int getRoomIDAtLocalBlock(int x, int y, int z) {
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

    /**
     * Sifts through the chunk data and removes any data pertaining to the
     * given room ID and then deletes the room ID from the key->ID table. If
     * the key for the room ID is not found in this chunk, then this does
     * nothing and simply returns.
     *
     * @param roomID The ID of the room to remove from this chunk
     */
    private void removeRoomIDFromKeys(int roomID) {
        int index = keyIndexForID(roomID);

        if(roomID < 0)
            return;

        for (int i = 0; i < _data.getLength(); i++) {
            if(_data.getValueAtIndex(i) == roomID)
                _data.setValueAtIndex(i, (byte)15);
        }

        _keyToId[index] = 0;
        _hasChangedSinceInit = true;
    }

    /**
     * Searches through the whole of the chunk data and removes any keys and ids
     * which aren't being utilised.
     *
     * @return The results of the cleanup, i.e. how many keys/IDs were cleaned/removed
     */
    public MirrorChunkCleanUpResults cleanUp() {
        MirrorChunkCleanUpResults results = new MirrorChunkCleanUpResults();

        /*
         * BooleanArray of all of the key slots that are found to be in use
         * by the data. If the key slot was not used, and it turns out there
         * is a key->id pair in the key array, then it will zeroed so that it
         * can be used for a new room if necessary.
         */
        BooleanArray foundKeys = new BooleanArray(MAX_UNIQUE_KEYS_PER_CHUNK);

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

    public class MirrorChunkKeysFullException extends Exception {}

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
