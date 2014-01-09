package com.shnud.noxray.World;

import com.shnud.noxray.Structures.ByteArraySection;
import com.shnud.noxray.Structures.NibbleArray;
import com.shnud.noxray.Structures.VariableBitArray;

/**
* Created by Andrew on 08/01/2014.
*/
public class MapChunkData {
    public static final int BLOCKS_PER_SECTION = 4096;
    private static final int BYTES_PER_PRIMARY_ID_SECTION = BLOCKS_PER_SECTION;
    private static final int BYTES_PER_ADDITIONAL_ID_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_METADATA_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_BLOCK_LIGHT_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_SKY_LIGHT_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_BIOME_SECTION = 256;

    private final int _x, _z;
    private final int addSectionAmount;
    private final int sectionAmount;
    private final ByteArraySection[] _primarySections = new ByteArraySection[16];
    private final NibbleArray[] _additionalSections = new NibbleArray[16];
    private final NibbleArray[] _metadataSections = new NibbleArray[16];
    private final NibbleArray[] _blockLightSections = new NibbleArray[16];

    public MapChunkData(int x, int z, byte[] data, short primary, short additional) {
        _x = x;
        _z = z;

        int count = 0;

        // First we simply need to count how many sections are in this chunk. This
        // will allow us to work out where the starting points for each of the data sections
        // are as we go the bitmask again

        for(int i = 0; i < 16; i++) count+= (primary >> i) & 1;

        sectionAmount = count;
        if(sectionAmount == 0) {
            // If there aren't any real sections of data then there's no point sifting through additional data because
            // it depends on the existence of primary block ID data
            addSectionAmount = 0;
            return;
        }

        // These offsets use the data-size of the previous section to work out where they are
        // e.g. the metadata offset uses the bits per block of the previous section to work out
        // where the metadata section should start

        // We divide by 8 each time to get the offset in bytes rather than bits, as we are currently
        // splitting the byte array into sections that can be used by the byte bit wrappers which will
        // abstract all of the byte splitting stuff away and just use indexes
        int metadataOffset = BYTES_PER_PRIMARY_ID_SECTION * sectionAmount;
        int blockLightOffset = metadataOffset + (BYTES_PER_METADATA_SECTION * sectionAmount);
        int additionalOffset = blockLightOffset + (BYTES_PER_BLOCK_LIGHT_SECTION * sectionAmount);

        count = 0;
        for(int i = 0; i < 16; i++) {
            if(((primary >> i) & 1) == 1) {

                _primarySections[i] =
                        new ByteArraySection(data, count * BLOCKS_PER_SECTION, BLOCKS_PER_SECTION);

                _metadataSections[i] =
                        new NibbleArray(new ByteArraySection(
                                data,
                                metadataOffset + (count * BYTES_PER_METADATA_SECTION),
                                BYTES_PER_METADATA_SECTION
                        ));

                _blockLightSections[i] =
                        new NibbleArray(new ByteArraySection(
                                data,
                                blockLightOffset + (count * BYTES_PER_BLOCK_LIGHT_SECTION),
                                BYTES_PER_BLOCK_LIGHT_SECTION
                        ));

                count++;
            }
        }

        // We don't need to count beforehand because we don't need to calculate the offset of
        // any other sections afterwards that rely on already knowing how many sections of
        // additional IDs there are
        count = 0;
        for(int i = 0; i < 16; i++) {
            if(((additional >> i) & 1) == 1) {
                _additionalSections[i] = new NibbleArray(new ByteArraySection(
                        data,
                        blockLightOffset + (BYTES_PER_ADDITIONAL_ID_SECTION * count),
                        BYTES_PER_ADDITIONAL_ID_SECTION
                ));

                count++;
            }
        }
        addSectionAmount = count;
    }

    public boolean isEmpty() {
        return sectionAmount == 0;
    }

    public int getX() {
        return _x;
    }

    public int getZ() {
        return _z;
    }

    public ByteArraySection getPrimaryIDSection(int i) {
        return _primarySections[i];
    }

    public NibbleArray getMetadataSection(int i) {
        return _metadataSections[i];
    }

    public NibbleArray getBlockLightSection(int i) {
        return _blockLightSections[i];
    }

    public NibbleArray getAdditionalIDSection(int i) {
        return _additionalSections[i];
    }
}
