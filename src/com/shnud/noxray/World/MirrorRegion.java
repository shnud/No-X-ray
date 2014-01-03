package com.shnud.noxray.World;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

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
    private File _worldFolder;
    private static final int PERIODIC_SAVE_INTERVAL_TICKS = 300 * MagicValues.MINECRAFT_TICKS_PER_SECOND;
    private BukkitTask _periodicSave;

    public MirrorRegion(int regionX, int regionZ, File worldFolder) {
        _x = regionX;
        _z = regionZ;
        _worldFolder = worldFolder;

        _minChunkX = regionX * MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _maxChunkX = _minChunkX + MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _minChunkZ = regionZ * MagicValues.HORIZONTAL_CHUNKS_IN_REGION;
        _maxChunkZ = _minChunkZ + MagicValues.HORIZONTAL_CHUNKS_IN_REGION;

        scheduleFutureSave();
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
        return _chunks[index];
    }

    /**
     * Load the chunks for this region from the MirrorWorld folder
     * @return the amount of chunks that were loaded
     * @throws IOException
     */
    public int loadFromFile() {
        File regionFile = new File(_worldFolder.getPath() + "/" + regionFileName());

        if(!regionFile.exists())
            return 0;

        int chunksLoaded = 0;

        try {
            RandomAccessFile ram = new RandomAccessFile(regionFile, "r");

            // For all possible 1024 chunks in this region
            for(int i = 0; i < 1024; i++) {
                // Is the chunk contained in the data? If not, it's empty
                if(ram.readBoolean()) {
                    int chunkX = _x * 32 + (i % 32);
                    int chunkZ = _z * 32 + (i / 32);
                    _chunks[i] = new MirrorChunk(chunkX, chunkZ);
                    _chunks[i].loadFromFile(ram);
                    chunksLoaded++;
                }
            }

            ram.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunksLoaded;
    }

    /**
     * Saves the region coordinates, and its containing chunks to the specified file. The file format
     * is as follows:
     *
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
     * @throws IOException
     */
    public void saveToFile() {
        String path = _worldFolder.getPath() + "/" + regionFileName();

        // We will use a temporary name so as not to
        // overide the old file straight away incase
        // something goes wrong while saving
        File newFile = new File(path + "temp");

        try {
            if(!newFile.exists())
                newFile.createNewFile();

            RandomAccessFile ram = new RandomAccessFile(newFile, "rw");
            // If the temp file somehow already exists, erase the file
            // and start at the beginning
            ram.setLength(0);

            for(MirrorChunk chunk : _chunks) {
                if(chunk != null && !chunk.isEmpty()) {
                    ram.writeBoolean(true);
                    chunk.saveToFile(ram);
                }
                else
                    ram.writeBoolean(false);
            }

            ram.close();

            // Now there is no way that we can't have finished
            // saving the region, so we can safely delete the old
            // file and rename this one to take its place
            File oldFile = new File(path);
            if(oldFile.exists())
                oldFile.delete();

            newFile.renameTo(oldFile);

            _periodicSave.cancel();

            /*
             * If this is being called from the periodic save, another
             * task will be scheduled straight away. If not, this prevents
             * from another save happening in the future after a region has
             * been saved due to being unloaded (no way for it to change
             * after being unloaded)
             */

        } catch (IOException e) {
            NoXray.getInstance().getLogger().log(Level.SEVERE, "Unable to save region: " + this + " to disk. Data may have been lost");
        }
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

    private String regionFileName() {
        return _x + "." + _z + ".mrr";
    }

    private void scheduleFutureSave() {
        _periodicSave = Bukkit.getScheduler().runTaskLater(NoXray.getInstance(), new PeriodicSave(), PERIODIC_SAVE_INTERVAL_TICKS);
    }

    private class PeriodicSave implements Runnable {
        public void run() {
            if(getChunksInUse() > 0) {
                saveToFile();
                scheduleFutureSave();
            }
        }
    }
}
