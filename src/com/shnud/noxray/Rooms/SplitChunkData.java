package com.shnud.noxray.Rooms;

import com.shnud.noxray.Structures.DynamicNibbleArray;
import com.shnud.noxray.Utilities.MagicValues;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 29/12/2013.
 */
public class SplitChunkData {

    private static final int DATA_SECTIONS = 8;
    private static final int BLOCKS_PER_SECTION = MagicValues.BLOCKS_IN_CHUNK / DATA_SECTIONS;
    private DynamicNibbleArray[] _sections = new DynamicNibbleArray[DATA_SECTIONS];

    private SplitChunkData() {}

    public static SplitChunkData createBlank() {
        return new SplitChunkData();
    }

    public static SplitChunkData createFromFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException, DataFormatException {
        SplitChunkData data = new SplitChunkData();
        data.readFromFileAtOffset(ram, fileOffset);
        return data;
    }

    public int getValueAtIndex(int index) {
        if(_sections[index / DATA_SECTIONS] == null)
            return 0;

        return _sections[index / DATA_SECTIONS].getValueAtIndex(index % BLOCKS_PER_SECTION);
    }

    public void setValueAtIndex(int index, byte value) {
        if(_sections[index / DATA_SECTIONS] == null)
            _sections[index / DATA_SECTIONS] = new DynamicNibbleArray(BLOCKS_PER_SECTION);

        _sections[index / DATA_SECTIONS].setValueAtIndex(index % BLOCKS_PER_SECTION, value);
    }

    public int getLength() {
        return MagicValues.BLOCKS_IN_CHUNK;
    }

    public void writeToFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException {
        ram.seek(fileOffset);

        for(DynamicNibbleArray section : _sections) {
            if(section == null) {
                ram.writeBoolean(false);
                continue;
            }

            ram.writeBoolean(true);
            byte[] compressed = section.getCompressedByteArray();
            ram.writeInt(compressed.length);
            ram.write(compressed);
        }
    }

    private void readFromFileAtOffset(RandomAccessFile ram, long fileOffset) throws IOException, DataFormatException {
        ram.seek(fileOffset);

        for(int i = 0; i < DATA_SECTIONS; i++) {
            if(ram.readBoolean()) {
                int compressedLength = ram.readInt();
                byte[] compressed = new byte[compressedLength];
                ram.readFully(compressed, 0, compressedLength);
                _sections[i] = DynamicNibbleArray.constructFromExistingCompressedArray(compressed, BLOCKS_PER_SECTION);
            }
        }
    }
}
