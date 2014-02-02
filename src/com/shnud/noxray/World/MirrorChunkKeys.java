package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.DynamicCoordinates;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorChunkKeys {

    private MirrorChunkSplitData _splitData = new MirrorChunkSplitData();

    public int getKeyAtBlock(DynamicCoordinates coords) {
        if(coords.getPrecisionLevel() != DynamicCoordinates.PrecisionLevel.BLOCK)
            throw new IllegalArgumentException("Coordinates are useless if not at block precision");

        return getKeyAtLocalBlock(coords.chunkRelativeBlockX(), coords.chunkRelativeBlockY(),
                coords.chunkRelativeBlockZ());
    }

    public int getKeyAtLocalBlock(int x, int y, int z) {
        int index = blockIndexFromLocalBlock(x, y, z);
        return getKeyAtIndex(index);
    }

    public int getKeyAtIndex(int index) {
        return _splitData.getValueAtIndex(index);
    }

    public void setKeyAtBlock(DynamicCoordinates coords, int key) {
        if(coords.getPrecisionLevel() != DynamicCoordinates.PrecisionLevel.BLOCK)
            throw new IllegalArgumentException("Coordinates are useless if not at block precision");

        int localX = coords.chunkRelativeBlockX();
        int localY = coords.chunkRelativeBlockY();
        int localZ = coords.chunkRelativeBlockZ();

        setKeyAtLocalBlock(localX, localY, localZ, key);
    }

    public void setKeyAtLocalBlock(int x, int y, int z, int key) {
        int index = blockIndexFromLocalBlock(x, y, z);
        setKeyAtIndex(index, key);
    }

    private void setKeyAtIndex(int index, int key) {
        _splitData.setValueAtIndex(index, key);
    }

    public void removeAllOfKey(int key) {
        Iterator<Integer> it = _splitData.iterator();

        while(it.hasNext()) {
            if(it.next() == key) it.remove();
        }
    }

    public void writeToFile(RandomAccessFile ram) throws IOException {
        _splitData.writeToFile(ram);
    }

    public void readFromFile(RandomAccessFile ram) throws IOException {
        _splitData.readFromFile(ram);
    }

    public boolean isEmpty() {
        return _splitData.isAllEmpty();
    }

    public boolean isMinecraftSectionEmpty(int section) {
        return _splitData.isMinecraftSectionEmpty(section);
    }

    private int blockIndexFromLocalBlock(int x, int y, int z) {
        return x + (z * 16) + (y * 256);
    }
}
