package com.shnud.noxray.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Structures.ByteArraySection;
import com.shnud.noxray.Structures.VariableBitArray;
import org.bukkit.entity.Player;

import java.util.zip.Deflater;

/**
 * Created by Andrew on 08/01/2014.
 */
public class MapChunkPacketEvent extends BasePacketEvent {

    /*
     * We should probably just use packet wrapper in the future
     */
    private final boolean _skylight;
    private final boolean _groundUp;

    public MapChunkPacketEvent(Player receiver, PacketEvent event) {
        super(receiver, event);

        if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
            _skylight = true;
            _groundUp = getPacket().getBooleans().read(0);
        }
        else {
            _skylight = getPacket().getBooleans().read(0);
            _groundUp = true;
        }
    }

    public MapChunkData getChunk(int i) {
        if(i > getAmountOfChunks() - 1) throw new ArrayIndexOutOfBoundsException(i);

        if (getPacket().getType() == PacketType.Play.Server.MAP_CHUNK) {
            byte[] data =  getPacket().getByteArrays().read(1);
            int x = getPacket().getIntegers().read(0);
            int z = getPacket().getIntegers().read(1);
            short primary = (short) getPacket().getIntegers().read(2).shortValue();
            short additional = (short) getPacket().getIntegers().read(3).shortValue();

            MapChunkData chunk = new MapChunkData(x, z, data, primary, additional);
            return chunk;
        }
        else {
            byte[] data = getPacket().getSpecificModifier(byte[][].class).read(0)[i];
            int x = getPacket().getIntegerArrays().read(0)[i];
            int z = getPacket().getIntegerArrays().read(1)[i];
            short primary = (short) getPacket().getIntegerArrays().read(2)[i];
            short additional = (short) getPacket().getIntegerArrays().read(3)[i];

            MapChunkData chunk = new MapChunkData(x, z, data, primary, additional);
            return chunk;
        }
    }

    private byte[] getByteArrayForChunk(int i) {
        if (getPacket().getType() == PacketType.Play.Server.MAP_CHUNK)
            return getPacket().getByteArrays().read(1);
        else
            return getPacket().getSpecificModifier(byte[][].class).read(0)[i];
    }

    public int getAmountOfChunks() {
        if(getPacket().getType() == PacketType.Play.Server.MAP_CHUNK)
            return 1;
        else
            return getPacket().getSpecificModifier(byte[][].class).read(0).length;
    }

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
        private final VariableBitArray[] _additionalSections = new VariableBitArray[16];
        private final VariableBitArray[] _metadataSections = new VariableBitArray[16];
        private final VariableBitArray[] _blockLightSections = new VariableBitArray[16];

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
                            new VariableBitArray(4, new ByteArraySection(
                                    data,
                                    metadataOffset + (count * BYTES_PER_METADATA_SECTION),
                                    BYTES_PER_METADATA_SECTION
                            ));

                    _blockLightSections[i] =
                            new VariableBitArray(4, new ByteArraySection(
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
                    _additionalSections[i] = new VariableBitArray(4, new ByteArraySection(
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

        public VariableBitArray getMetadataSection(int i) {
            return _metadataSections[i];
        }

        public VariableBitArray getBlockLightSection(int i) {
            return _blockLightSections[i];
        }

        public VariableBitArray getAdditionalIDSection(int i) {
            return _additionalSections[i];
        }
    }

    public void compressDataForSendingToClient() {
        Deflater def = new Deflater();

        if(getPacket().getType() == PacketType.Play.Server.MAP_CHUNK) {

            def.setInput(getPacket().getByteArrays().read(1));
            def.finish();
            int deflatedLength = def.deflate(getPacket().getByteArrays().read(0));
            getPacket().getIntegers().write(4, deflatedLength);
        }
        else {
            // Combine all of the chunks in one long byte array, then compress
            // it into the compressed byte buffer

            byte[] allChunks = getPacket().getByteArrays().read(1);
            int runningOffset = 0;
            for(int i = 0; i < getAmountOfChunks(); i++) {
                byte[] thisChunk = getByteArrayForChunk(i);
                System.arraycopy(thisChunk, 0, allChunks, runningOffset, thisChunk.length);
                runningOffset += thisChunk.length;
            }

            byte[] compressedBuffer = getPacket().getByteArrays().read(0);
            if(compressedBuffer == null)
                return;

            //compressedBuffer = new byte[runningOffset];

            def.setInput(allChunks);
            def.finish();

            // Set the length of the compressed byte array
            int deflatedLength = def.deflate(compressedBuffer);
            getPacket().getIntegers().write(0, deflatedLength);
        }

        def.end();
    }
}
