package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener, MirrorChunkEventListener {

    private MirrorRegionMap _regionMap;
    private RoomList _rooms;
    private World _world;
    private File _worldFolder;
    private static final long MILLISECONDS_TO_WAIT_BETWEEN_POSSIBLE_REGION_SAVES_AFTER_CHUNK_CHANGE = 120 * 1000;

    public MirrorWorld(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _worldFolder = new File(NoXray.getInstance().getDataFolder().getPath() + "/" + _world.getName() + "/");
        createWorldDirectoryIfNotExist();
        _rooms = new RoomList(this);

        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    public File getFolder() { return _worldFolder; }

    private void createWorldDirectoryIfNotExist() {
        if(!_worldFolder.isDirectory())
            _worldFolder.mkdir();
    }

    private void loadRegion(int x, int z) {
        if(_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = new MirrorRegion(x, z, this);
        _regionMap.putRegion(region);

        File regionFile = new File(_worldFolder.getPath() + "/" + regionFileName(x, z));

        if(!regionFile.exists())
            return;

        try {
            region.loadFromFile(regionFile);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MirrorRegion.WrongRegionException e) {
            e.printStackTrace();
        }
    }

    private void unloadRegion(int x, int z) {
        if(!_regionMap.containsRegion(x, z))
            return;

        MirrorRegion toUnload = _regionMap.getRegion(x, z);
        saveRegion(x, z);
        _regionMap.removeRegion(x, z);
    }

    private void saveRegion(int x, int z) {
        if(!_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = _regionMap.getRegion(x, z);
        String path = _worldFolder.getPath() + "/" + regionFileName(x, z);

        File newFile = new File(path + "temp");
        try {
            region.saveToFile(newFile);
            File oldFile = new File(path);

            if(oldFile.exists())
                oldFile.delete();

            newFile.renameTo(oldFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        DynamicCoordinates coords = DynamicCoordinates.initWithChunkCoordinates(event.getChunk().getX(), 0, event.getChunk().getZ());

        if(!_regionMap.containsRegion(coords.regionX(), coords.regionZ()))
            loadRegion(coords.regionX(), coords.regionZ());

        _regionMap.getRegion(coords.regionX(), coords.regionZ()).chunkInUse();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        DynamicCoordinates coords = DynamicCoordinates.initWithChunkCoordinates(event.getChunk().getX(), 0, event.getChunk().getZ());
        _regionMap.getRegion(coords.regionX(), coords.regionZ()).chunkNotInUse();

        if(_regionMap.getRegion(coords.regionX(), coords.regionZ()).getChunksInUse() == 0)
            unloadRegion(coords.regionX(), coords.regionZ());
    }

    private static String regionFileName(int x, int z) {
        return x + "." + z + ".mrr";
    }

    public String getName() {
        return _world.getName();
    }

    public RoomList getRooms() {
        return _rooms;
    }

    @Override
    public void chunkChangeEvent(int x, int z) {

    }

    @Override
    public void roomAddedToChunkEvent(int roomID, int x, int z) {

    }

    @Override
    public void roomRemovedFromChunkEvent(int roomID, int x, int z) {

    }
}
