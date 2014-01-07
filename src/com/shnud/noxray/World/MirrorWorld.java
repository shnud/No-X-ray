package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.io.File;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener {

    private static final long MILLISECONDS_TO_WAIT_BETWEEN_POSSIBLE_REGION_SAVES_AFTER_CHUNK_CHANGE = 120 * 1000;
    private final MirrorRegionMap _regionMap;
    private final World _world;
    private final File _worldFolder;

    public MirrorWorld(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _worldFolder = new File(NoXray.getInstance().getDataFolder().getPath() + "/" + _world.getName() + "/");
        _regionMap = new MirrorRegionMap();

        if(!_worldFolder.isDirectory())
            _worldFolder.mkdir();

        Chunk[] loadedChunks = world.getLoadedChunks();
        for(Chunk chunk : loadedChunks) loadChunk(chunk.getX(), chunk.getZ());

        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    public File getFolder() { return _worldFolder; }

    private void loadRegion(int x, int z) {
        if(_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = new MirrorRegion(x, z, this._worldFolder);
        region.loadFromFile();
        _regionMap.putRegion(region);
    }

    private void unloadRegion(int x, int z) {
        if(!_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = _regionMap.getRegion(x, z);
        region.saveToFile();
        _regionMap.removeRegion(x, z);
    }

    public void loadChunk(int x, int z) {
        int regionX = x >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = z >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        if(!_regionMap.containsRegion(regionX, regionZ))
            loadRegion(regionX, regionZ);

        _regionMap.getRegion(regionX, regionZ).retain();
    }

    public void unloadChunk(int x, int z) {
        int regionX = x >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = z >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        if(!_regionMap.containsRegion(regionX, regionZ))
            return;

        MirrorRegion region = _regionMap.getRegion(regionX, regionZ);
        region.release();

        if(region.chunksInUse() <= 0) {
            unloadRegion(regionX, regionZ);
        }
    }

    public String getWorldName() {
        return _world.getName();
    }

    public void saveAllRegions() {
        for(MirrorRegion region : _regionMap) {
            region.saveToFile();
        }
    }

    public int getRoomIDAtBlock(int x, int y, int z) {
        DynamicCoordinates coordinates = DynamicCoordinates.initWithBlockCoordinates(x, y, z);
        return getRoomIDAtBlock(coordinates);
    }

    public int getRoomIDAtBlock(DynamicCoordinates coordinates) {
        return _regionMap.getRegion(coordinates.regionX(), coordinates.regionZ()).getChunk(coordinates).getRoomIDAtBlock(coordinates);
    }

    public boolean setRoomIDAtBlock(int x, int y, int z, int roomID) {
        return setRoomIDAtBlock(DynamicCoordinates.initWithBlockCoordinates(x, y, z), roomID);
    }

    public boolean setRoomIDAtBlock(DynamicCoordinates coordinates, int roomID) {
        MirrorChunk chunk = getChunk(coordinates);
        if(!chunk.isEmpty() && !chunk.containsRoomID(roomID) && chunk.isFull())
            chunk.cleanUp();

        return chunk.setBlockToRoomID(coordinates, roomID);
    }

    /**
     * Will return null if chunk isn't loaded
     */
    public MirrorChunk getChunk(DynamicCoordinates coordinates) {
        if(!_regionMap.containsRegion(coordinates.regionX(), coordinates.regionZ()))
            return null;

        return _regionMap.getRegion(coordinates.regionX(), coordinates.regionZ()).getChunk(coordinates);
    }
}
