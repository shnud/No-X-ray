package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.MagicValues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorRegion {

    private MirrorChunk[] _chunks = new MirrorChunk[MagicValues.CHUNKS_IN_REGION];
    private int _regionX, _regionZ;
    private int _minChunkX, _maxChunkX, _minChunkZ, _maxChunkZ;
    private int _chunksInUse = 0;

    private MirrorRegion(int regionX, int regionZ) {
        _regionX = regionX;
        _regionZ = regionZ;

        _minChunkX = regionX * MagicValues.CHUNKS_IN_REGION;
        _maxChunkX = _minChunkX + 32;
        _minChunkZ = regionZ * MagicValues.CHUNKS_IN_REGION;
        _maxChunkZ = _minChunkZ + 32;
    }

    /**
     * Attemps to read in chunks from a MirrorRegion file
     *
     * @throws java.io.IOException If the file specified couldn't be found
     * @param regionFile The file where the MirrorRegion is located
     * @return The newly created MirrorRegion object
     */
    public static MirrorRegion initFromFile(int regionX, int regionZ, File regionFile) throws IOException, WrongRegionException, DataFormatException {
        if(!regionFile.exists())
            throw new FileNotFoundException("Region file does not exist");

        MirrorRegion region = new MirrorRegion(regionX, regionZ);

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

                region._chunks[i] = MirrorChunk.constructFromFileAtOffset(chunkX, chunkZ, ram, ram.getFilePointer());
            }
            i++;
        }

        ram.close();
        return region;
    }

    /**
     * Returns a new Blank MirrorRegion object
     *
     * @param regionX The x coordinate of the region
     * @param regionZ The z coordinate of the region
     *
     * @return The newly created MirrorRegion object
     *
     */
    public static MirrorRegion createBlank(int regionX, int regionZ) {
        return new MirrorRegion(regionX, regionZ);
    }

    private static int getChunkIndex(int chunkX, int chunkZ) {
        return chunkX % MagicValues.CHUNKS_IN_REGION + ((chunkZ % MagicValues.CHUNKS_IN_REGION) * MagicValues.CHUNKS_IN_REGION);
    }

    public static String regionFileName(int regionX, int regionZ) {
        return regionX + "." + regionZ + ".mrr";
    }

    public String regionFileName() {
        return regionFileName(_regionX, _regionZ);
    }

    public void retain() { _chunksInUse++; }

    public void release() { _chunksInUse--; }

    /**
     * The retain count is used for containing world object to keep
     * track of how many chunks are relying on this region to be loaded.
     */
    public int getRetainCount() { return _chunksInUse; }

    /**
     * Get the chunk located at the given global chunk coordinates
     *
     * @param chunkX the global chunk x coordinate to get
     * @param chunkZ the global chunk z coordinate to get
     * @return either the already existing loaded chunk, or a new chunk if it hasn't been initiated yet
     * @throws java.lang.ArrayIndexOutOfBoundsException if the chunk doesn't exist within this region
     */
    public MirrorChunk getChunk(int chunkX, int chunkZ) {
        if(chunkX < _minChunkX || chunkX > _maxChunkX || chunkZ < _minChunkZ || chunkZ > _maxChunkZ)
            throw new ArrayIndexOutOfBoundsException("Chunk does not exist within this region");

        int index = getChunkIndex(chunkX, chunkZ);

        if(_chunks[index] == null)
            _chunks[index] = MirrorChunk.constructBlankMirrorChunk(chunkX, chunkZ);

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
