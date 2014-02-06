package com.shnud.noxray.Packets.PacketHelpers;

import com.shnud.noxray.Structures.ByteArraySection;
import com.shnud.noxray.Structures.ByteWrappers.NibbleArray;

/**
 * Created by Andrew on 08/01/2014.
 */
public class MapChunkDataWrapper {
    public static final int BLOCKS_PER_SECTION = 4096;
    private static final int BYTES_PER_PRIMARY_ID_SECTION = BLOCKS_PER_SECTION;
    private static final int BYTES_PER_ADDITIONAL_ID_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_METADATA_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_BLOCK_LIGHT_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_SKY_LIGHT_SECTION = BLOCKS_PER_SECTION / 2;
    private static final int BYTES_PER_BIOME_SECTION = 256;

    private final byte[] _rawData;
    private final int _x, _z;
    private final short _primaryBitmask, _additionalBitmask;
    private final int _addSectionAmount;
    private final int _sectionAmount;
    private final ByteArraySection[] _primarySections = new ByteArraySection[16];
    private final NibbleArray[] _additionalSections = new NibbleArray[16];
    private final NibbleArray[] _metadataSections = new NibbleArray[16];
    private final NibbleArray[] _blockLightSections = new NibbleArray[16];

    public MapChunkDataWrapper(int x, int z, byte[] data, short primaryBitmask, short additionalBitmask) {
        _x = x;
        _z = z;
        _rawData = data;
        _primaryBitmask = primaryBitmask;
        _additionalBitmask = additionalBitmask;

        int sectionCount = 0;

        // First we simply need to count how many sections are in this chunk. This
        // will allow us to work out where the starting points for each of the data sections
        // are as we go the bitmask again

        for(int i = 0; i < 16; i++) sectionCount+= (primaryBitmask >> i) & 1;

        _sectionAmount = sectionCount;
        if(_sectionAmount == 0) {
            // If there aren't any real sections of data then there's no point sifting through additional data because
            // it depends on the existence of primary block ID data
            _addSectionAmount = 0;
            return;
        }

        // These offsets use the data-size of the previous section to work out where they are
        // e.g. the metadata offset uses the bits per block of the previous section to work out
        // where the metadata section should start

        // We divide by 8 each time to get the offset in bytes rather than bits, as we are currently
        // splitting the byte array into sections that can be used by the byte bit wrappers which will
        // abstract all of the byte splitting stuff away and just use indexes
        int metadataOffset = BYTES_PER_PRIMARY_ID_SECTION * _sectionAmount;
        int blockLightOffset = metadataOffset + (BYTES_PER_METADATA_SECTION * _sectionAmount);
        int additionalOffset = blockLightOffset + (BYTES_PER_BLOCK_LIGHT_SECTION * _sectionAmount);

        sectionCount = 0;
        for(int i = 0; i < 16; i++) {
            if(((primaryBitmask >> i) & 1) == 1) {

                _primarySections[i] =
                        new ByteArraySection(

                                data,
                                sectionCount * BLOCKS_PER_SECTION, BLOCKS_PER_SECTION
                        );

                _metadataSections[i] =
                        new NibbleArray(
                                new ByteArraySection(

                                        data,
                                        metadataOffset + (sectionCount * BYTES_PER_METADATA_SECTION),
                                        BYTES_PER_METADATA_SECTION
                                ),

                                NibbleArray.NibbleOrder.EVEN_ON_LEFT
                        );

                _blockLightSections[i] =
                        new NibbleArray(
                                new ByteArraySection(

                                        data,
                                        blockLightOffset + (sectionCount * BYTES_PER_BLOCK_LIGHT_SECTION),
                                        BYTES_PER_BLOCK_LIGHT_SECTION
                                ),

                                NibbleArray.NibbleOrder.EVEN_ON_LEFT
                        );

                sectionCount++;
            }
        }

        // We don't need to count beforehand because we don't need to calculate the offset of
        // any other sections afterwards that rely on already knowing how many sections of
        // additional IDs there are
        sectionCount = 0;
        for(int i = 0; i < 16; i++) {
            if(((additionalBitmask >> i) & 1) == 1) {
                _additionalSections[i] = new NibbleArray(
                        new ByteArraySection(

                                data,
                                additionalOffset + (BYTES_PER_ADDITIONAL_ID_SECTION * sectionCount),
                                BYTES_PER_ADDITIONAL_ID_SECTION
                        ),

                        NibbleArray.NibbleOrder.EVEN_ON_LEFT
                );

                sectionCount++;
            }
        }
        _addSectionAmount = sectionCount;
    }

    public boolean isEmpty() {
        return _sectionAmount == 0;
    }

    public int getX() {
        return _x;
    }

    public int getZ() {
        return _z;
    }

    public short getPrimaryBitmask() {
        return _primaryBitmask;
    }

    public short getAdditionalBitmask() {
        return _additionalBitmask;
    }

    public byte[] getRawData() {
        return _rawData;
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
