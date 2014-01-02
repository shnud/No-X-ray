package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Structures.HashMapArrayList;
import com.shnud.noxray.Utilities.MagicValues;
import com.shnud.noxray.Utilities.XZ;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener, MirrorChunkEventListener {

    private MirrorRegionMap _regionMap;
    private HashMapArrayList<Integer, Room> _rooms = new HashMapArrayList<Integer, Room>();
    private World _world;
    private File _worldFolder;
    private static final long MILLISECONDS_TO_WAIT_BETWEEN_POSSIBLE_REGION_SAVES_AFTER_CHUNK_CHANGE = 120 * 1000;

    public MirrorWorld(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _worldFolder = new File(NoXray.getInstance().getDataFolder().getPath() + "/" + _world.getName() + "/");
        createDirectoryIfNotExist();

        try {
            loadRooms();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to load rooms file, we don't know which chunks rooms are in");
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    private void loadRooms() throws IOException {
        File roomData = new File(_worldFolder.getPath() + "/" + "roomData");

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
        File roomData = new File(_worldFolder.getPath() + "/" + "roomDatatemp");
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

        File oldFile = new File(_worldFolder.getPath() + "/" + "roomData");
        if(oldFile.exists())
            oldFile.delete();

        roomData.renameTo(oldFile);
    }

    private void createDirectoryIfNotExist() {
        if(!_worldFolder.isDirectory())
            _worldFolder.mkdir();
    }

    private boolean isRegionLoaded(int x, int z) {
        return _regionMap.containsRegion(x, z);
    }

    private void loadRegion(int regionX, int regionZ) {
        if(isRegionLoaded(regionX, regionZ)) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to load a region which was already loaded: [" + regionX + ", " + regionZ + "]");
            return;
        }

        File regionFile = new File(_worldFolder.getPath() + "/" + MirrorRegion.regionFileName(regionX, regionZ));

        if(regionFile.exists()) {

            try {
                MirrorRegion region = MirrorRegion.initFromFile(regionX, regionZ, regionFile);
                _regionMap.putRegion(region);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (MirrorRegion.WrongRegionException e) {
                e.printStackTrace();
            }
        } else {
            _regionMap.putRegion(MirrorRegion.createBlank(regionX, regionZ));
        }
    }

    private void unloadRegion(int regionX, int regionZ) {
        if(!isRegionLoaded(regionX, regionZ)) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to unload a region which wasn't already loaded: [" + regionX + ", " + regionZ + "]");
            return;
        }
        try {
            saveRegion(_regionMap.getRegion(regionX, regionZ));
            _regionMap.removeRegion(regionX, regionZ);

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error while writing region [" + regionX + ", " + regionZ + "] to disk. Data may have been lost");
            Bukkit.getLogger().log(Level.WARNING, "Region was not unloaded from mirror world");
            e.printStackTrace();
        }
    }

    private MirrorRegion getRegion(int regionX, int regionZ) {
        return _regionMap.getRegion(regionX, regionZ);
    }

    private void saveRegion(MirrorRegion region) throws IOException {
        String path = _worldFolder.getPath() + "/" + region.regionFileName();

        File newFile = new File(path + "temp");
        region.saveToFile(newFile);

        File oldFile = new File(path);

        if(oldFile.exists())
            oldFile.delete();

        newFile.renameTo(oldFile);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        Chunk chunk = event.getChunk();

        int regionX = event.getChunk().getX() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = event.getChunk().getZ() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        getRegion(regionX, regionZ).retain();
        getRegion(regionX, regionZ).setChunkListener(chunk.getX(), chunk.getZ(), this);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        int regionX = event.getChunk().getX() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = event.getChunk().getZ() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        getRegion(regionX, regionZ).release();

        if(getRegion(regionX, regionZ).getRetainCount() == 0)
            unloadRegion(regionX, regionZ);
    }

    @Override
    public void chunkChangeEvent(int x, int z) {
        int regionX = x >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = z >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        if(isRegionLoaded(regionX, regionZ)) {
            MirrorRegion region = getRegion(regionX, regionZ);
            if(region.getMillisecondsSinceLastSuccessfulSave() > MILLISECONDS_TO_WAIT_BETWEEN_POSSIBLE_REGION_SAVES_AFTER_CHUNK_CHANGE) {
                try {
                    saveRegion(region);
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Unable to save region [" + regionX + ", " + regionZ + "] on periodic save");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void roomAddedToChunkEvent(int roomID, int x, int z) {
        if(!_rooms.containsKey(roomID))
            _rooms.add(roomID, new Room(roomID));

        _rooms.get(roomID).addChunk(new XZ(x, z));
    }

    @Override
    public void roomRemovedFromChunkEvent(int roomID, int x, int z) {
        if(!_rooms.containsKey(roomID))
            return;

        _rooms.get(roomID).removeChunk(new XZ(x, z));
    }
}
