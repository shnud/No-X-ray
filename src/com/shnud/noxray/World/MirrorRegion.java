package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorRegion {

    private MirrorChunk[] _chunks = new MirrorChunk[MagicValues.CHUNKS_IN_REGION];
    private int _x, _z;
    private int _minChunkX, _maxChunkX, _minChunkZ, _maxChunkZ;
    private int _chunksInUse = 0;
    private long _timeOfLastSuccessfulSave = 0;
    private MirrorWorld _world;

    public MirrorRegion(int regionX, int regionZ, MirrorWorld world) {
        _x = regionX;
        _z = regionZ;
        _world = world;

        _minChunkX = regionX * MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _maxChunkX = _minChunkX + MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _minChunkZ = regionZ * MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _maxChunkZ = _minChunkZ + MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
    }

    private static int getChunkIndex(int chunkX, int chunkZ) {
        return chunkX % MagicValues.HORIZONTAL_CHUNKS_IN_REGION + ((chunkZ % MagicValues.HORIZONTAL_CHUNKS_IN_REGION) * MagicValues.HORIZONTAL_CHUNKS_IN_REGION);
    }

    public MirrorChunk getChunk(DynamicCoordinates coordinates) {
        int chunkX = coordinates.chunkX();
        int chunkZ = coordinates.chunkZ();

        if(chunkX < _minChunkX || chunkX > _maxChunkX || chunkZ < _minChunkZ || chunkZ > _maxChunkZ)
            throw new ArrayIndexOutOfBoundsException("Chunk does not exist within this region");

        int index = getChunkIndex(chunkX, chunkZ);
        if(_chunks[index] == null)
            _chunks[index] = new MirrorChunk(chunkX, chunkZ);

        _chunks[index].setListener(_world);
        return _chunks[index];
    }

    public void loadFromFile(File regionFile) throws IOException, WrongRegionException {

        if(!regionFile.exists())
            throw new FileNotFoundException("Region file does not exist");

        RandomAccessFile ram = new RandomAccessFile(regionFile, "r");
        int x = ram.readInt();
        int z = ram.readInt();

        // Don't know why this would ever happen, stupid really
        if(x != _x || z != _z)
            throw new WrongRegionException();

        // For all possible 1024 chunks in this region
        for(int i = 0; i < 1024; i++) {
            // Is the chunk contained in the data? If not, it's empty
            if(ram.readBoolean()) {
                int chunkX = _x * 32 + (i % 32);
                int chunkZ = _z * 32 + (i / 32);
                _chunks[i] = new MirrorChunk(chunkX, chunkZ);
                _chunks[i].loadFromFileAtOffset(ram, ram.getFilePointer());
            }
        }

        ram.close();

    }

    /**
     * Saves the region coordinates, and its containing chunks to the specified file. The file format
     * is as follows:
     *
     * - Two integers specifying x and z coordinates of region
     * - For each chunk: (1024x)
     *
     *    - Boolean value specifying whether the next chunk is included in the file
     *    - Time last cleaned up as a long (if greater than a certain amount will be cleaned before loading)
     *    - Unsigned byte specifying how many roomIDs to expect in the chunk
     *    - Integers for each roomIDs contained in the chunk
     *    - For each section of chunk: (8x)
     *
     *       - Boolean value specifying whether the section is empty
     *       - If not empty, int value specifying length in bytes of compressed section
     *       - Compressed byte array of the keys in that section
     *
     * Chunks are ordered with the x changing fastest, e.g. [0, 0], [1, 0], [2, 0], [3, 0]
     *
     * @param regionFile
     * @throws IOException
     */
    public void saveToFile(File regionFile) throws IOException {
        if(!regionFile.exists())
            regionFile.createNewFile();

        RandomAccessFile ram = new RandomAccessFile(regionFile, "rw");

        ram.setLength(0);
        ram.writeInt(_x);
        ram.writeInt(_z);

        for(MirrorChunk chunk : _chunks) {
            if(chunk != null && !chunk.isEmpty()) {
                ram.writeBoolean(true);
                chunk.saveToFileAtOffset(ram, ram.getFilePointer());
            }
            else
                ram.writeBoolean(false);
        }

        ram.close();
        _timeOfLastSuccessfulSave = System.currentTimeMillis();
    }

    public long getMillisecondsSinceLastSuccessfulSave() {
        return System.currentTimeMillis() - _timeOfLastSuccessfulSave;
    }

    public int getX() {
        return _x;
    }

    public int getZ() {
        return _z;
    }

    public void chunkInUse() { _chunksInUse++; }

    public void chunkNotInUse() { _chunksInUse--; }

    public int getChunksInUse() { return _chunksInUse; }

    public static class ChunkNotLoadedException extends Exception {}

    public static class WrongRegionException extends Exception {}

    public String toString() {
        return "MirrorRegion[" + _x + ", " + _z + "]";
    }
}
