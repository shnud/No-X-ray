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

    private HashMap<Integer, Room> _rooms = new HashMap<Integer, Room>();
    private MirrorWorld _world;
    private int highestKnownRoom = 1;

    public RoomList(MirrorWorld world) {
        _world = world;
        loadRooms();
    }

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

    public Room getRoomFromID(int roomID) {
        return _rooms.get(roomID);
    }

    public void addKnownChunkToRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            addRoom(new Room(roomID));

        _rooms.get(roomID).addChunk(new XZ(chunkX, chunkZ));
    }

    public void removeKnownChunkFromRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            return;

        _rooms.remove(roomID);
    }

    public HashSet<XZ> getKnownChunksForRoom(int roomID) {
        if(!_rooms.containsKey(roomID))
            return new HashSet<XZ>();

        return _rooms.get(roomID).getKnownChunks();
    }

    public void removeRoom(int roomID) {
        if(_rooms.containsKey(roomID))
            _rooms.remove(roomID);
    }

    public int getUnusedRoomID() {
        return highestKnownRoom + 1;
    }

    public void addRoom(Room room) {
        if(room.getID() > highestKnownRoom)
            highestKnownRoom = room.getID();

        _rooms.put(room.getID(), room);
    }
}
