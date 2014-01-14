package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.logging.Level;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener {

    private static final long MILLISECONDS_TO_WAIT_BETWEEN_POSSIBLE_REGION_SAVES_AFTER_CHUNK_CHANGE = 120 * 1000;
    private final MirrorRegionMap _regionMap;
    private final String _worldName;
    private final File _worldFolder;

    public MirrorWorld(final World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        /*
         * We don't store the world here because access to it would have to be synchronized as this class
         * is never accessed off of the main thread but instead the room hiding thread for the given world
         */

        _worldName = world.getName();
        _worldFolder = new File(NoXray.getInstance().getDataFolder().getPath() + "/" + _worldName + "/");
        _regionMap = new MirrorRegionMap();

        if(!_worldFolder.isDirectory())
            _worldFolder.mkdir();


        /*
         * Make sure that if we have just reloaded the plugin or it has only just been enabled after
         * the server has been up for a while, that we load all the chunks that we have missed events for
         */

        Chunk[] loadedChunks = world.getLoadedChunks();
        for(Chunk chunk : loadedChunks) loadMirrorChunk(chunk.getX(), chunk.getZ());



        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    public File getFolder() { return _worldFolder; }

    private void loadMirrorRegion(int x, int z) {
        if(_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = new MirrorRegion(x, z, this._worldFolder);
        region.loadFromFile();
        _regionMap.putRegion(region);
    }

    private void unloadMirrorRegion(int x, int z) {
        if(!_regionMap.containsRegion(x, z))
            return;

        MirrorRegion region = _regionMap.getRegion(x, z);
        region.saveToFile();
        _regionMap.removeRegion(x, z);
    }

    public void loadMirrorChunk(int x, int z) {
        int regionX = x >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = z >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        if(!_regionMap.containsRegion(regionX, regionZ))
            loadMirrorRegion(regionX, regionZ);

        _regionMap.getRegion(regionX, regionZ).retain();
    }

    public void unloadMirrorChunk(int x, int z) {
        int regionX = x >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;
        int regionZ = z >> MagicValues.BITSHIFTS_RIGHT_CHUNK_TO_REGION;

        if(!_regionMap.containsRegion(regionX, regionZ))
            return;

        MirrorRegion region = _regionMap.getRegion(regionX, regionZ);
        region.release();

        if(region.chunksInUse() <= 0) {
            unloadMirrorRegion(regionX, regionZ);
        }
    }

    public String getWorldName() {
        return _worldName;
    }

    public void saveAllRegions() {
        NoXray.getInstance().getLogger().log(Level.INFO, "Saving mirror world for world: " + _worldName);
        for(MirrorRegion region : _regionMap) {
            region.saveToFile();
        }
    }

    public int getRoomIDAtBlock(int x, int y, int z) {
        DynamicCoordinates coordinates = DynamicCoordinates.initWithBlockCoordinates(x, y, z);
        return getRoomIDAtBlock(coordinates);
    }

    public int[] getRoomIDAtBlockAndAdjacent(int x, int y, int z) {
        int[] rooms = new int[7];
        rooms[0] = getRoomIDAtBlock(x, y, z);
        rooms[1] = getRoomIDAtBlock(x - 1, y, z);
        rooms[2] = getRoomIDAtBlock(x + 1, y, z);
        rooms[3] = getRoomIDAtBlock(x, y - 1, z);
        rooms[4] = getRoomIDAtBlock(x, y + 1, z);
        rooms[5] = getRoomIDAtBlock(x, y, z - 1);
        rooms[6] = getRoomIDAtBlock(x, y, z + 1);
        return rooms;
    }

    public int[] getRoomIDAtBlockAndAdjacent(DynamicCoordinates coords) {
        return getRoomIDAtBlockAndAdjacent(coords.blockX(), coords.blockY(), coords.blockZ());
    }

    public int getRoomIDAtBlock(DynamicCoordinates coordinates) {
        return _regionMap.getRegion(coordinates.regionX(), coordinates.regionZ()).getChunk(coordinates).getRoomIDAtBlock(coordinates);
    }

    public boolean setRoomIDAtBlock(int x, int y, int z, int roomID) {
        return setRoomIDAtBlock(DynamicCoordinates.initWithBlockCoordinates(x, y, z), roomID);
    }

    public boolean setRoomIDAtBlock(DynamicCoordinates coordinates, int roomID) {
        MirrorChunk chunk = getMirrorChunk(coordinates);
        if(!chunk.isEmpty() && !chunk.containsRoomID(roomID) && chunk.isFull())
            chunk.cleanUp();

        return chunk.setBlockToRoomID(coordinates, roomID);
    }

    /**
     * Will return null if chunk isn't loaded
     */
    public MirrorChunk getMirrorChunk(DynamicCoordinates coordinates) {
        if(!_regionMap.containsRegion(coordinates.regionX(), coordinates.regionZ()))
            return null;

        return _regionMap.getRegion(coordinates.regionX(), coordinates.regionZ()).getChunk(coordinates);
    }

    public MirrorChunk getMirrorChunk(int x, int z) {
        return getMirrorChunk(DynamicCoordinates.initWithChunkCoordinates(x, 0, z));
    }

    public boolean isMirrorChunkLoaded(DynamicCoordinates coordinates) {
        if(!_regionMap.containsRegion(coordinates.regionX(), coordinates.regionZ()))
            return false;

        return _regionMap.getRegion(coordinates.regionX(), coordinates.regionZ()).isMirrorChunkLoaded(coordinates);
    }

    public boolean isMirrorChunkLoaded(int x, int z) {
        return isMirrorChunkLoaded(DynamicCoordinates.initWithChunkCoordinates(x, 0, z));
    }


}
