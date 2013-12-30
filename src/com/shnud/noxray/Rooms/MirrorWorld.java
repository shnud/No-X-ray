package com.shnud.noxray.Rooms;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener {

    private HashMap<String, MirrorRegion> _regionMap = new HashMap<String, MirrorRegion>();
    private World _world;
    private File _worldFolder;

    public MirrorWorld(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _worldFolder = new File(NoXray.getInstance().getDataFolder().getPath() + "/" + _world.getName() + "/");
        createDirectoryIfNotExist();

        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    private void createDirectoryIfNotExist() {
        if(!_worldFolder.isDirectory())
            _worldFolder.mkdir();
    }

    public boolean containsRegion(int x, int z) {
        return _regionMap.containsKey(keyFromCoordinates(x, z));
    }

    public void loadRegion(int regionX, int regionZ) {
        if(containsRegion(regionX, regionZ)) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to load a region which was already loaded: [" + regionX + ", " + regionZ + "]");
            return;
        }

        File regionFile = new File(_worldFolder.getPath() + "/" + MirrorRegion.regionFileName(regionX, regionZ));
        String key = keyFromCoordinates(regionX, regionZ);

        if(regionFile.exists()) {

            try {
                MirrorRegion region = MirrorRegion.initFromFile(_world, regionX, regionZ, regionFile);
                _regionMap.put(key, region);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (MirrorRegion.WrongRegionException e) {
                e.printStackTrace();
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
        } else {
            _regionMap.put(key, MirrorRegion.createBlank(_world, regionX, regionZ));
        }
    }

    public void unloadRegion(int regionX, int regionZ) {
        if(!containsRegion(regionX, regionZ)) {
            Bukkit.getLogger().log(Level.WARNING, "Tried to unload a region which wasn't already loaded: [" + regionX + ", " + regionZ + "]");
            return;
        }

        String path = _worldFolder.getPath() + "/" + MirrorRegion.regionFileName(regionX, regionZ);
        File newFile = new File(path + "temp");
        String key = keyFromCoordinates(regionX, regionZ);

        try {
            _regionMap.get(key).saveToFile(newFile);

            File oldFile = new File(path);

            if(oldFile.exists())
                oldFile.delete();

            newFile.renameTo(oldFile);

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error while writing region [" + regionX + ", " + regionZ + "] to disk. Data may have been lost");
            e.printStackTrace();
        }

        _regionMap.remove(key);
    }

    private MirrorRegion getRegion(int regionX, int regionZ) {
        String key = keyFromCoordinates(regionX, regionZ);

        if(!_regionMap.containsKey(key))
            loadRegion(regionX, regionZ);

        return _regionMap.get(key);
    }

    private static String keyFromCoordinates(int x, int z) {
        return x + ":" + z;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        int regionX = event.getChunk().getX() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = event.getChunk().getZ() >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        getRegion(regionX, regionZ).retain();
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
}
