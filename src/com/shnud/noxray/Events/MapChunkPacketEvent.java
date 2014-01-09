package com.shnud.noxray.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.World.MapChunkData;
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
