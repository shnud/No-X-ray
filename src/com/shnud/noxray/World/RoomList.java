package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Structures.HashMapArrayList;
import com.shnud.noxray.Utilities.XZ;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by Andrew on 02/01/2014.
 */
public class RoomList {

    private HashMapArrayList<Integer, Room> _rooms = new HashMapArrayList<Integer, Room>();
    private MirrorWorld _world;

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
                Room newRoom = new Room(roomID);

                do {
                    int chunkX = ram.readInt();
                    int chunkZ = ram.readInt();

                    newRoom.addChunk(new XZ(chunkX, chunkZ));
                } while(ram.readChar() == ',');

                _rooms.put(newRoom.getID(), newRoom);
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

            for(Room room : _rooms) {
                ram.writeInt(room.getID());

                ArrayList<XZ> chunks = room.getListOfKnownChunks();

                for(int i = 0; i < chunks.size(); i++) {
                    ram.writeInt(chunks.get(i).x);
                    ram.writeInt(chunks.get(i).z);

                    if(i == chunks.size() - 1)
                        ram.writeChar(';');
                    else
                        ram.writeChar(',');
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
            _rooms.put(roomID, new Room(roomID));

        _rooms.get(roomID).addChunk(new XZ(chunkX, chunkZ));
    }

    public void removeKnownChunkFromRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            return;

        _rooms.remove(roomID);
    }

    public ArrayList<XZ> getKnownChunksForRoom(int roomID) {
        if(!_rooms.containsKey(roomID))
            return new ArrayList<XZ>();

        return _rooms.get(roomID).getListOfKnownChunks();
    }
}
