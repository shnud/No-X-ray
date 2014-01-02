package com.shnud.noxray.World;

import com.shnud.noxray.Structures.DynamicByteArray;
import com.shnud.noxray.Structures.DynamicByteBitWrapper;
import com.shnud.noxray.Utilities.MagicValues;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 29/12/2013.
 */
public class MirrorChunkData {

    private static final int DATA_SECTIONS = 8;
    private static final int BLOCKS_PER_SECTION = MagicValues.BLOCKS_IN_CHUNK / DATA_SECTIONS;
    private static final int DEFAULT_BIT_PER_VALUE_ENCODING = 2;
    private static final int MAX_BIT_PER_VALUE_ENCODING = 6;
    private DynamicByteBitWrapper[] _sections = new DynamicByteBitWrapper[DATA_SECTIONS];
    private boolean _isEmpty = true;

    private MirrorChunkData() {}

    public static MirrorChunkData createBlank() {
        return new MirrorChunkData();
    }

    public static MirrorChunkData createFromFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException, DataFormatException {
        MirrorChunkData data = new MirrorChunkData();
        data.readFromFileAtOffset(ram, fileOffset);
        return data;
    }

    public int getValueAtIndex(int index) {
        int sectionIndex = index / DATA_SECTIONS;

        if(_sections[sectionIndex] == null)
            return 0;

        return _sections[sectionIndex].getValueAtIndex(index % BLOCKS_PER_SECTION);
    }

    public void setValueAtIndex(int index, int value) throws ValueTooLargeForEncodingException {
        int sectionIndex = index / DATA_SECTIONS;

        if(_sections[sectionIndex] == null)
            _sections[sectionIndex] = new DynamicByteBitWrapper(DEFAULT_BIT_PER_VALUE_ENCODING, BLOCKS_PER_SECTION);


        DynamicByteBitWrapper section = _sections[sectionIndex];
        while(value > section.maxValue()) {
            if(section.maxValue() >= MAX_BIT_PER_VALUE_ENCODING)
                throw new ValueTooLargeForEncodingException();

            section.convertTo(section.getBitsPerVal() + 1);
        }

        _sections[sectionIndex].setValueAtIndex(index % BLOCKS_PER_SECTION, value);
        _isEmpty = false;
    }

    public void writeToFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException {
        ram.seek(fileOffset);

        for(DynamicByteBitWrapper section : _sections) {

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

    public void readFromFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException {
        ram.seek(fileOffset);

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

                _sections[i] = new DynamicByteBitWrapper(bitValueLength, array);
            }
        }
    }

    public boolean isEmpty() {
        if(_isEmpty)
            return true;

        for(DynamicByteBitWrapper section : _sections) {
            if(section != null)
                return false;
        }

        return true;
    }

    public class ValueTooLargeForEncodingException extends Exception {}
}
