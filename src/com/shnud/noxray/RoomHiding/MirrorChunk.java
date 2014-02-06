package com.shnud.noxray.RoomHiding;

import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.XYZ;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * An object that stores a key->value map of contained room IDs and uses the keys in a structure very similar to standard minecraft chunk data to keep track of which blocks are hidden.
 */
public class MirrorChunk {

    // Whether we should try and clean up the chunk (see cleanUp()) before we return that a new room could not be added
    private static final boolean SHOULD_ATTEMPT_CLEANUP_BEFORE_ASSUMING_KEYS_ARE_FULL = true;

    // The data where we store keys for each block in the chunk. These are associated with a roomID in the key->ID map
    private final MirrorChunkKeys _data;

    // The key->ID map where we store the keys for all the contained room IDs
    private final MirrorChunkValues _keyToIDMap;

    // The chunk x and z coordinates for the Minecraft chunk that this is associated with
    private final int _x, _z;

    // The time we last attempted to clean up the chunk (see cleanUp())
    private long _timeOfLastCleanUp = 0;

    public MirrorChunk(int x, int z) {
        _x = x;
        _z = z;
        _data = new MirrorChunkKeys();
        _keyToIDMap = new MirrorChunkValues();
    }

    /**
     * Save this mirror chunk to a file
     * @param ram the file (file pointer must already be at the correct location for reading)
     * @throws IOException
     */
    public void saveToFile(RandomAccessFile ram) throws IOException {
        ram.writeLong(_timeOfLastCleanUp);

        int[] slots = _keyToIDMap.getSlots();
        ram.writeByte(slots.length - 1);

        for(int slot = 1; slot < slots.length; slot++) {
            ram.writeInt(slots[slot]);
        }

        _data.writeToFile(ram);
    }

    /**
     * Load this mirror chunk from a file
     * @param ram the file (file pointer must already be at the correct location for reading)
     * @throws IOException
     */
    public void loadFromFile(RandomAccessFile ram) throws IOException {
        _timeOfLastCleanUp = ram.readLong();

        int keySlotAmount = ram.readByte();

        for(int i = 1; i < keySlotAmount + 1; i++) {
            int id = ram.readInt();
            _keyToIDMap.setSlotToID(i, id);
        }

        _data.readFromFile(ram);
    }

    /**
     * Set the room ID at a given block to a specified ID
     *
     * @param coordinates the coordinates of the block (dynamic coordinates can work out the local chunk relative index from global block coordinates)
     * @param roomID the room ID to set it to
     * @return whether setting the block to the room was successful
     */
    public boolean setBlockToRoomID(DynamicCoordinates coordinates, int roomID) {
        if(roomID < 0)
            throw new IllegalArgumentException("Room ID must be 0 (not a room) or greater");

        int key;

        // If the room key is 0 then the caller wants to set the block to not-a-room
        // Otherwise if we don't know of the roomID in this chunk, add it to the key->id map
        // If that wasn't possible, return false (the map may be full)
        // and we could attempt to clean up the data

        if(roomID != 0 && !_keyToIDMap.containsRoomID(roomID)) {
            key = _keyToIDMap.addRoomID(roomID);
            if(key < 0)
                return false;
        }
        else
            key = _keyToIDMap.getKeyForRoomID(roomID);

        _data.setKeyAtBlock(coordinates, key);
        return true;
    }

    /**
     * Get the room ID of a block at the given coordinates
     * @param coordinates the coordinates of the block (dynamic coordinates can work out the local chunk relative index from global block coordinates)
     * @return the room ID located at the coordinates (0 if no room)
     */
    public int getRoomIDAtBlock(DynamicCoordinates coordinates) {
        int key = _data.getKeyAtBlock(coordinates);
        return _keyToIDMap.getRoomIDForKey(key);
    }

    /**
     * Get the room ID of a block at the given block index
     * @param index the index of the block (block indexes are the same as for standard minecraft chunks: x + (z * 16) + (y * 256))
     * @return the room ID located at the index (0 if no room)
     */
    public int getRoomIDAtIndex(int index) {
        int key = _data.getKeyAtIndex(index);
        return _keyToIDMap.getRoomIDForKey(key);
    }

    /**
     * Get all of the blocks for any given room contained within this chunk
     * @param roomID the room ID to look for
     * @return a list of chunk-relative coordinates of all blocks in this chunk pertaining to a given room ID
     */
    public List<XYZ> getAllBlocksForRoomID(int roomID) {
        List<XYZ> blocks = new ArrayList<XYZ>();

        for(int section = 0; section < 16; section++) {
            if(isSectionEmpty(section))
                continue;

            int x = 0;
            int y = section * 16;
            int z = 0;

            int secStart = section * 4096;
            int secFinish = secStart + 4096;

            for(int blockIndex = secStart; blockIndex < secFinish; blockIndex++) {
                if(getRoomIDAtIndex(blockIndex) == roomID)
                    blocks.add(new XYZ(x, y, z));

                if(x == 15) {
                    x = 0;
                    if(z == 15) {
                        y++;
                        z = 0;
                    }
                    else
                        z++;
                }
                else
                    x++;
            }
        }
        return blocks;
    }

    /**
     * Remove the specified room from this chunk
     * @param roomID the room ID to remove
     */
    public void removeRoom(int roomID) {
        int key = _keyToIDMap.getKeyForRoomID(roomID);

        // If we know the room is contained within this chunk, then go through the data
        // and remove all references to it
        if(key > 0) {
            _data.removeAllOfKey(key);
            _keyToIDMap.removeRoomID(roomID);
        }
    }

    /**
     * Whether this chunk is completely empty of room blocks
     * @return true if the chunk contains no room blocks, false if it does
     */
    public boolean isEmpty() {
        /*
         * Both of these are useless without each other, so if either one is empty,
         * we can report that the chunk is empty.
         */
        return _data.isEmpty() || _keyToIDMap.isEmpty();
    }

    /**
     * Whether this chunk's key->id map is full (mirror chunks can only store so many unique rooms so as to save the amount of
     * space required for one block in the data)
     * @return true if it is, false if it's not
     */
    public boolean isFull() {
        return _keyToIDMap.isFull();
    }

    /**
     * Returns whether this chunk contains blocks for a certain room ID
     * @param roomID the room ID to check for
     * @return false if this chunk contains no key->id pair for the room ID, true if it does
     */
    public boolean containsRoomID(int roomID) {
        return _keyToIDMap.containsRoomID(roomID);
    }

    /**
     * Sift through the data and the keys and remove and keys that are not in use both from the key->id map and the data.
     *
     * When rooms are removed from the chunk every block that was protected should really be removed as from the data as
     * well as the key from the key->id map but this function may come in useful in the event that for some reason that
     * doesn't happen and we need to add more rooms to the chunk.
     */
    public void cleanUp() {
        // TODO

        _timeOfLastCleanUp = System.currentTimeMillis();
    }

    /**
     * Returns whether the section of this chunk (exactly like the standard minecraft chunk) is empty of hidden blocks
     * @param section the section (0 - 15 inclusive to check)
     * @return true if the section is empty, false if not
     */
    public boolean isSectionEmpty(int section) {
        if(section < 0 || section > 15)
            throw new IllegalArgumentException("Section must be between 0 and 15 inclusive");

        return _data.isMinecraftSectionEmpty(section);
    }
}
