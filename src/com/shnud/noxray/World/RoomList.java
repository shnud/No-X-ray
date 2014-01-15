package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.XZ;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by Andrew on 02/01/2014.
 */
public class RoomList {

    private final HashMap<Integer, Room> _rooms = new HashMap<Integer, Room>();
    private final MirrorWorld _world;
    private int highestKnownRoom = 1;

    public RoomList(MirrorWorld world) {
        _world = world;
        loadRooms();
    }

    /**
     * Load all of the rooms in the room list from a 'roomData' file within the plugin>world folder
     */
    private void loadRooms() {
        File roomData = new File(_world.getFolder().getPath() + "/" + "roomData");

        if(!roomData.exists()) {
            NoXray.getInstance().getLogger().log(Level.INFO, "Room data was not found for world \"" + _world.getWorldName() + "\"");
            NoXray.getInstance().getLogger().log(Level.INFO, "New room data will be saved");
            return;
        }

        try {
            RandomAccessFile ram = new RandomAccessFile(roomData, "r");

            while(ram.getFilePointer() < ram.length()) {
                int roomID = ram.readInt();
                int records = ram.readInt();
                Room newRoom = new Room(roomID);

                for (int i = 0; i < records; i++) {
                    int chunkX = ram.readInt();
                    int chunkZ = ram.readInt();
                    newRoom.addChunk(new XZ(chunkX, chunkZ));
                }

                addRoom(newRoom);
            }

            ram.close();

        } catch (IOException e) {
            NoXray.getInstance().getLogger().log(Level.WARNING, "Unable to load room data for world " + _world.getWorldName() + "\"");
            NoXray.getInstance().getLogger().log(Level.WARNING, "We do not know which chunks rooms are in");
        }
    }

    /**
     * Save all of the rooms in the room list to a 'roomData' file within the plugin>world folder
     */
    public void saveRooms() {
        if(_rooms.isEmpty())
            return;

        String path = _world.getFolder().getPath() + "/" + "roomData";
        File roomData = new File(path + "temp");

        try {
            RandomAccessFile ram = new RandomAccessFile(roomData, "rw");

            for(Integer room : _rooms.keySet()) {
                HashSet<XZ> chunks = _rooms.get(room).getKnownChunks();
                // If the known chunks are empty don't bother saving the room to
                // the file as it's a waste of space
                if(chunks.isEmpty())
                    continue;

                ram.writeInt(room);
                // Write integer specifying amount of chunk coordinate pairs to expect
                ram.writeInt(chunks.size());
                for (XZ chunk : chunks) {
                    ram.writeInt(chunk.x);
                    ram.writeInt(chunk.z);
                }
            }

            ram.close();

            File oldFile = new File(path);
            if(oldFile.exists())
                oldFile.delete();

            roomData.renameTo(oldFile);

        } catch (IOException e) {
            NoXray.getInstance().getLogger().log(Level.WARNING, "Unable to save room data");
            NoXray.getInstance().getLogger().log(Level.WARNING, "Next run we may not know which chunks rooms are in");
        }
    }

    /**
     * Get the room object for the given room ID
     * @param roomID the room ID of the room
     * @return the room object for the given ID
     */
    public Room getRoomFromID(int roomID) {
        return _rooms.get(roomID);
    }

    /**
     * Let the room know that it is now contained within a certain chunk
     * @param chunkX the chunk x coordinate (in chunk coordinates)
     * @param chunkZ the chunk z coordinate (in chunk coordinates)
     * @param roomID the room ID of the room
     */
    public void addKnownChunkToRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            addRoom(new Room(roomID));

        _rooms.get(roomID).addChunk(new XZ(chunkX, chunkZ));
    }

    /**
     * Let the room know that it is no longer contained within a certain chunk
     * @param chunkX the chunk x coordinate (in chunk coordinates)
     * @param chunkZ the chunk z coordinate (in chunk coordinates)
     * @param roomID the room ID of the room
     */
    public void removeKnownChunkFromRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            return;

        _rooms.remove(roomID);
    }

    /**
     * Get every chunk that the given room is known to be contained within
     *
     * @param roomID the room ID of the room
     * @return a HashSet of all the known chunks
     */
    public HashSet<XZ> getKnownChunksForRoom(int roomID) {
        if(!_rooms.containsKey(roomID))
            return new HashSet<XZ>();

        return _rooms.get(roomID).getKnownChunks();
    }

    /**
     * Remove a room from the list of rooms
     * @param roomID the roomID of the room to remove
     */
    public void removeRoom(int roomID) {
        if(_rooms.containsKey(roomID))
            _rooms.remove(roomID);
    }

    /**
     * Remove a room from the list of rooms
     * @param room the room to remove
     */
    public void removeRoom(Room room) {
        if(_rooms.containsKey(room.getID()))
            _rooms.remove(room.getID());
    }

    public int getUnusedRoomID() {
        return highestKnownRoom + 1;
    }

    /**
     * Add a new room to the list of rooms
     *
     * Note that adding a new room must always go through this function, because it
     * keeps track of the highest room ID that we've seen
     * @param room the new room to add, ID must be greater than 0
     */
    public void addRoom(Room room) {
        if(room.getID() < 1)
            throw new IllegalArgumentException("Room ID cannot be less than 1");

        if(room.getID() > highestKnownRoom)
            highestKnownRoom = room.getID();

        _rooms.put(room.getID(), room);
    }
}
