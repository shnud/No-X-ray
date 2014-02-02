package com.shnud.noxray.World;

import com.shnud.noxray.Structures.ByteArray;
import com.shnud.noxray.Structures.ByteWrappers.VariableBitsizeUnsignedArray;
import com.shnud.noxray.Structures.DeflatingByteArray;
import com.shnud.noxray.Structures.DynamicBitsizeUnsignedArray;
import com.shnud.noxray.Utilities.MagicValues;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class MirrorChunkSplitData implements Iterable<Integer> {

    private static final int NUMBER_OF_SECTIONS = 8;
    private static final int BLOCKS_PER_SECTION = MagicValues.BLOCKS_IN_CHUNK / NUMBER_OF_SECTIONS;
    private final DynamicBitsizeUnsignedArray[] _sections = new DynamicBitsizeUnsignedArray[NUMBER_OF_SECTIONS];

    public int getValueAtIndex(int index) {
        DynamicBitsizeUnsignedArray section = getSectionFromBlockIndex(index);

        if(section == null)
            return 0;

        return section.getValueAtIndex(getIndexWithinSection(index));
    }

    public void setValueAtIndex(int index, int value) {
        if(getSectionFromBlockIndex(index) == null) {
            if(value == 0)
                return;

            createSection(getSectionIndexFromBlockIndex(index));
        }

        getSectionFromBlockIndex(index).setValueAtIndex(getIndexWithinSection(index), value);
    }

    public boolean isAllEmpty() {
        for(int i = 0; i < NUMBER_OF_SECTIONS; i++) if(!isSectionEmpty(i)) return false;

        return true;
    }

    public boolean isMinecraftSectionEmpty(int section) {
        return isSectionEmpty(section / 16 / NUMBER_OF_SECTIONS);
    }

    public boolean isSectionEmpty(int section) {
        return _sections[section] == null || _sections[section].isEmpty();
    }

    public void writeToFile(RandomAccessFile ram) throws IOException {
        for(DynamicBitsizeUnsignedArray section : _sections) {
            // If this section of the chunk is NOT contained within the data
            // write boolean:false to the file and continue to the next section
            if(section == null || section.isEmpty()) {
                ram.writeBoolean(false);
                continue;
            }

            // This section of the chunk is contained within the data
            ram.writeBoolean(true);

            // The bits per value encoding of the bit/byte wrapper
            ram.writeByte(section.getCurrentBitsPerValue());

            // The length of the compressed data, and then the actual compressed byte array of data
            byte[] uncompressed = section.getByteArray().getPrimitiveByteArray();
            byte[] compressed = DeflatingByteArray.compressAndReturnResult(uncompressed);

            ram.writeInt(compressed.length);
            ram.write(compressed);
        }
    }

    public void readFromFile(RandomAccessFile ram) throws IOException {
        for(int i = 0; i < NUMBER_OF_SECTIONS; i++) {
            if(ram.readBoolean()) {
                // The bits per value encoding of the bit/byte wrapper
                byte bitValueLength = ram.readByte();

                // The length of the following compressed data
                int compressedLength = ram.readInt();

                // The actual compressed data
                byte[] compressed = new byte[compressedLength];
                ram.readFully(compressed, 0, compressedLength);
                byte[] uncompressed = DeflatingByteArray.uncompressAndReturnResult(compressed);

                _sections[i] = new DynamicBitsizeUnsignedArray(

                        new VariableBitsizeUnsignedArray(
                                bitValueLength,
                                new ByteArray(uncompressed),
                                BLOCKS_PER_SECTION
                        )
                );
            }
        }
    }

    public Iterator<Integer> iterator() {
        return new KeyDataIterator();
    }

    private class KeyDataIterator implements Iterator<Integer> {
        private int _index = -1;

        @Override
        public boolean hasNext() {
            return _index + 1 < MagicValues.BLOCKS_IN_CHUNK;
        }

        @Override
        public Integer next() {
            return getValueAtIndex(++_index);
        }

        @Override
        public void remove() {
            setValueAtIndex(_index, 0);
        }
    }

    private void testBlockIndexIsValid(int index) {
        if(index < 0 || index >= MagicValues.BLOCKS_IN_CHUNK)
            throw new IllegalArgumentException("Index is out of chunk block index bounds (0 - 65535)");
    }

    private DynamicBitsizeUnsignedArray getSectionFromBlockIndex(int index) {
        testBlockIndexIsValid(index);
        return _sections[getSectionIndexFromBlockIndex(index)];
    }

    private int getSectionIndexFromBlockIndex(int index) {
        return index / BLOCKS_PER_SECTION;
    }

    private int getIndexWithinSection(int index) {
        testBlockIndexIsValid(index);
        return index % BLOCKS_PER_SECTION;
    }

    private void createSection(int section) {
        _sections[section] = new DynamicBitsizeUnsignedArray(BLOCKS_PER_SECTION);
    }
}
