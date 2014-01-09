package com.shnud.noxray.World;

import com.shnud.noxray.Structures.DynamicVariableBitArray;
import com.shnud.noxray.Structures.DynamicByteArray;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorChunkKeyData {

    private static final int DATA_SECTIONS = 8;
    private static final int DATA_SECTIONS_PER_REAL_CHUNK_SECTION = 16 / DATA_SECTIONS;
    private static final int BLOCKS_PER_SECTION = MagicValues.BLOCKS_IN_CHUNK / DATA_SECTIONS;
    private static final int DEFAULT_BIT_PER_VALUE_ENCODING = 2;
    private static final int MAX_BIT_PER_VALUE_ENCODING = 6;
    private final DynamicVariableBitArray[] _sections = new DynamicVariableBitArray[DATA_SECTIONS];
    private boolean _isEmpty = true;

    private MirrorChunkKeyData() {}

    public static MirrorChunkKeyData createBlank() {
        return new MirrorChunkKeyData();
    }

    private int blockIndexFromLocalCoords(int x, int y, int z) {
        return x + (z * 16) + (y * 256);
    }

    public int getBlockKey(DynamicCoordinates coords) {
        if(coords.getPrecisionLevel() != DynamicCoordinates.PrecisionLevel.BLOCK)
            throw new IllegalArgumentException("Coordinates are useless if not at block precision");

        return getLocalBlockKey(coords.chunkRelativeBlockX(), coords.chunkRelativeBlockY(), coords.chunkRelativeBlockZ());
    }

    public int getLocalBlockKey(int x, int y, int z) {
        int index = blockIndexFromLocalCoords(x, y, z);

        return getValueAtIndex(index);
    }

    public int getValueAtIndex(int index) {
        int sectionIndex = index / BLOCKS_PER_SECTION;

        if(_sections[sectionIndex] == null)
            return 0;

        return _sections[sectionIndex].getValueAtIndex(index % BLOCKS_PER_SECTION);
    }

    public void setBlockKey(DynamicCoordinates coords, int roomID) {
        if(coords.getPrecisionLevel() != DynamicCoordinates.PrecisionLevel.BLOCK)
            throw new IllegalArgumentException("Coordinates are useless if not at block precision");

        int localX = coords.chunkRelativeBlockX();
        int localY = coords.chunkRelativeBlockY();
        int localZ = coords.chunkRelativeBlockZ();

        setLocalBlockKey(localX, localY, localZ, roomID);
    }

    public void setLocalBlockKey(int x, int y, int z, int roomID) {
        int index = blockIndexFromLocalCoords(x, y, z);

        setValueAtIndex(index, roomID);
    }

    private void setValueAtIndex(int index, int value) {
        int sectionIndex = index / BLOCKS_PER_SECTION;

        if(_sections[sectionIndex] == null)
            _sections[sectionIndex] = new DynamicVariableBitArray(DEFAULT_BIT_PER_VALUE_ENCODING, BLOCKS_PER_SECTION);


        DynamicVariableBitArray section = _sections[sectionIndex];
        while(value > section.maxValue()) {
            if(section.maxValue() >= MAX_BIT_PER_VALUE_ENCODING)
                throw new IllegalArgumentException("Value too large for encoding");

            section.convertTo(section.getBitsPerVal() + 1);
        }

        _sections[sectionIndex].setValueAtIndex(index % BLOCKS_PER_SECTION, value);
        _isEmpty = false;
    }

    public void removeAllKeys(int roomID) {
        for(DynamicVariableBitArray section : _sections) {
            if(section == null)
                continue;

            for(int i = 0; i < section.size(); i++) {
                if(section.getValueAtIndex(i) == roomID)
                    section.setValueAtIndex(i, 0);
            }
        }
    }

    public void writeToFile(RandomAccessFile ram) throws IOException {
        for(DynamicVariableBitArray section : _sections) {

            // If this section of the chunk is NOT contained within the data
            if(section == null) {
                ram.writeBoolean(false);
                continue;
            }
            // This section of the chunk is contained within the data
            ram.writeBoolean(true);

            // The bits per value encoding of the bit/byte wrapper
            ram.writeByte(section.getBitsPerVal());

            // The length of the compressed data, and then the actual compressed byte array of data
            byte[] compressed = section.getByteArray().getCompressedPrimitiveByteArray();
            ram.writeInt(compressed.length);
            ram.write(compressed);
        }
    }

    public void readFromFile(RandomAccessFile ram) throws IOException {
        for(int i = 0; i < DATA_SECTIONS; i++) {
            if(ram.readBoolean()) {
                // If we're reading a section of data then we can assume that the data isn't empty
                _isEmpty = false;

                // The bits per value encoding of the bit/byte wrapper
                byte bitValueLength = ram.readByte();

                // The length of the following compressed data
                int compressedLength = ram.readInt();

                // The actual compressed data
                byte[] compressed = new byte[compressedLength];
                ram.readFully(compressed, 0, compressedLength);
                DynamicByteArray array = new DynamicByteArray(compressed, true);

                _sections[i] = new DynamicVariableBitArray(bitValueLength, array);
            }
        }
    }

    public boolean isEmpty() {
        if(_isEmpty)
            return true;

        for(DynamicVariableBitArray section : _sections) {
            if(section != null)
                return false;
        }

        return true;
    }

    public boolean isSectionEmpty(int section) {
        return _sections[section / DATA_SECTIONS_PER_REAL_CHUNK_SECTION] == null;
    }
}
