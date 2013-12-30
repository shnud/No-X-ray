package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorRegion {

    private MirrorChunk[] _chunks = new MirrorChunk[MagicValues.CHUNKS_IN_REGION];
    private World _world;
    private int _regionX, _regionZ;
    private int _chunksInUse = 0;

    private MirrorRegion(World world, int regionX, int regionZ) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _regionX = regionX;
        _regionZ = regionZ;
    }

    /**
     * Attemps to read in chunks from a MirrorRegion file
     *
     * @throws java.io.IOException If the file specified couldn't be found
     * @param regionFile The file where the MirrorRegion is located
     * @return The newly created MirrorRegion object
     */
    public static MirrorRegion initFromFile(World world, int regionX, int regionZ, File regionFile) throws IOException, WrongRegionException, DataFormatException {
        if(!regionFile.exists())
            throw new FileNotFoundException("Region file does not exist");

        MirrorRegion region = new MirrorRegion(world, regionX, regionZ);

        /*
         * File format for MirrorRegion:
         *
         * - Two integers specifying x and z coordinates of region
         * - For each chunk: (1024x)
         *
         *    - Boolean value specifying whether the next chunk is included in the file
         *    - 15 integers specifying the roomIDs which are contained in this chunk
         *    - For each section of chunk: (8x)
         *
         *       - Boolean value specifying whether the section is empty
         *       - If not empty, int value specifying length of compressed section
         *       - Compressed byte array of the keys in that section
         *
         * Chunks are ordered with the x changing fastest, e.g. [0, 0], [1, 0], [2, 0], [3, 0]
         */

        RandomAccessFile ram = new RandomAccessFile(regionFile, "r");
        int x = ram.readInt();
        int z = ram.readInt();

        if(x != region._regionX || z != region._regionZ)
            throw new WrongRegionException();

        int i = 0;
        while(i < 1024) {
            if(ram.readBoolean()) {
                int chunkX = region._regionX * 32 + (i % 32);
                int chunkZ = region._regionZ * 32 + (i / 32);

                region._chunks[i] = MirrorChunk.constructFromFileAtOffset(region._world, chunkX, chunkZ, ram, ram.getFilePointer());
            }
            i++;
        }

        ram.close();
        return region;
    }

    /**
     * Returns a new Blank MirrorRegion object
     *
     * @param world The world this region is part of
     * @param regionX The x coordinate of the region
     * @param regionZ The z coordinate of the region
     *
     * @return The newly created MirrorRegion object
     *
     */
    public static MirrorRegion createBlank(World world, int regionX, int regionZ) {
        return new MirrorRegion(world, regionX, regionZ);
    }

    private static int getChunkIndex(int chunkX, int chunkZ) {
        return chunkX % MagicValues.CHUNKS_IN_REGION + ((chunkZ % MagicValues.CHUNKS_IN_REGION) * MagicValues.CHUNKS_IN_REGION);
    }

    public static String regionFileName(int regionX, int regionZ) {
        return regionX + "." + regionZ + ".mrr";
    }

    public void retain() { _chunksInUse++; }

    public void release() { _chunksInUse--; }

    public int getRetainCount() { return _chunksInUse; }

    private MirrorChunk getChunk(int chunkX, int chunkZ) throws ChunkNotLoadedException {
        int index = getChunkIndex(chunkX, chunkZ);

        if(_chunks[index] == null)
            throw new ChunkNotLoadedException();

        return _chunks[index];
    }

    private boolean isChunkLoaded(int chunkX, int chunkZ) {
        int index = getChunkIndex(chunkX, chunkZ);
        return _chunks[index] != null;
    }

    public void saveToFile(File regionFile) throws IOException {
        if(!regionFile.exists())
            regionFile.createNewFile();

        RandomAccessFile ram = new RandomAccessFile(regionFile, "rw");
        ram.setLength(0);
        ram.writeInt(_regionX);
        ram.writeInt(_regionZ);

        for(MirrorChunk chunk : _chunks) {
            if(chunk != null) {
                ram.writeBoolean(true);
                chunk.saveToFileAtOffset(ram, ram.getFilePointer());
            }
            else
                ram.writeBoolean(false);
        }

        ram.close();
    }

    public static class ChunkNotLoadedException extends Exception {}
    public static class WrongRegionException extends Exception {}
}
