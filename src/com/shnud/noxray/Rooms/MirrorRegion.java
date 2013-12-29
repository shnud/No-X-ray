package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.World;

import java.io.File;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorRegion {

    private MirrorChunk[] _chunks = new MirrorChunk[MagicValues.CHUNKS_IN_REGION];
    private World _world;
    private int _x, _z;
    private int _chunksInUse = 0;8
    public void retain() { _chunksInUse++; }
    public void release() { _chunksInUse--; }
    public int getRetainCount() { return _chunksInUse; }

    public static MirrorRegion createFromFile(World world, int regionX, int regionZ, File regionFile) {
        MirrorRegion region = new MirrorRegion(world, regionX, regionZ);

        if(!regionFile.exists())
            throw new IllegalArgumentException("Region file does not exist");



        return null;
    }

    public static MirrorRegion createBlank(World world, int regionX, int regionZ) {
        return new MirrorRegion(world, regionX, regionZ);
    }

    /*
     * Uses global region and chunk coordinates
     * Region coords = block coords >> 9
     * Chunk coords = block coords >> 4
     */
    private MirrorRegion(World world, int regionX, int regionZ) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _x = regionX;
        _z = regionZ;
    }

    public void loadChunk(int chunkX, int chunkZ) {
        int index = getChunkIndex(chunkX, chunkZ);
    }

    public void unloadChunk(int chunkX, int chunkZ) {
        int index = getChunkIndex(chunkX, chunkZ);
    }

    private MirrorChunk getChunk(int chunkX, int chunkZ) throws ChunkNotLoadedException {
        int index = getChunkIndex(chunkX, chunkZ);

        if(_chunks[index] == null)
            throw new ChunkNotLoadedException();

        return _chunks[index];
    }

    private static int getChunkIndex(int chunkX, int chunkZ) {
        return chunkX % MagicValues.CHUNKS_IN_REGION + ((chunkZ % MagicValues.CHUNKS_IN_REGION) * MagicValues.CHUNKS_IN_REGION);
    }

    public static String regionFileName(int regionX, int regionZ) {
        return regionX + "." + regionZ + ".mrr";
    }

    private boolean isChunkLoaded(int chunkX, int chunkZ) {
        int index = getChunkIndex(chunkX, chunkZ);
        return _chunks[index] != null;
    }

    public class ChunkNotLoadedException extends Exception {}
}
