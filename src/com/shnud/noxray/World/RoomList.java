package com.shnud.noxray.World;

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

        try {
            loadRooms();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to load list of rooms, we don't know which chunks they're in");
        }
    }

    private void loadRooms() throws IOException {
        File roomData = new File(_world.getFolder().getPath() + "/" + "roomData");

        if(!roomData.exists()) {
            Bukkit.getLogger().log(Level.INFO, "Room data was not found for world \"" + _world.getName() + "\"");
            Bukkit.getLogger().log(Level.INFO, "New room data will be saved");
            return;
        }

        RandomAccessFile ram = new RandomAccessFile(roomData, "r");

        while(ram.getFilePointer() < ram.length()) {
            int roomID = ram.readInt();
            Room newRoom = new Room(roomID);

            do {
                int chunkX = ram.readInt();
                int chunkZ = ram.readInt();

                newRoom.addChunk(new XZ(chunkX, chunkZ));
            } while(ram.readChar() == ',');

            _rooms.add(newRoom.getID(), newRoom);
        }

        ram.close();
    }

    private void saveRooms() throws IOException {
        File roomData = new File(_world.getFolder().getPath() + "/" + "roomDatatemp");
        RandomAccessFile ram = new RandomAccessFile(roomData, "rw");

        if(_rooms.isEmpty())
            return;

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

        File oldFile = new File(_world.getFolder().getPath() + "/" + "roomData");
        if(oldFile.exists())
            oldFile.delete();

        roomData.renameTo(oldFile);
    }

    public Room getRoomFromID(int roomID) {
        return _rooms.get(roomID);
    }

    public void addKnownChunkToRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            _rooms.add(roomID, new Room(roomID));

        _rooms.get(roomID).addChunk(new XZ(chunkX, chunkZ));
    }

    public void removeKnownChunkFromRoom(int chunkX, int chunkZ, int roomID) {
        if(!_rooms.containsKey(roomID))
            return;

        _rooms.remove(roomID);
    }
}
