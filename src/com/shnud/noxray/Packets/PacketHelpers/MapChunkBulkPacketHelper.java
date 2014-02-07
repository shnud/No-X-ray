package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunkBulk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.shnud.noxray.NoXray;

import java.util.zip.Deflater;

public class MapChunkBulkPacketHelper extends AbstractMapChunkPacketHelper {

    public MapChunkBulkPacketHelper(WrapperPlayServerMapChunkBulk packet) {
        super(packet);
    }

    @Override
    public MapChunkDataWrapper getMutableChunk(int i) {
        if(i >= getAmountOfChunks())
            throw new ArrayIndexOutOfBoundsException(i);

        return new MapChunkDataWrapper(
                getWrappedPacket().getChunksX()[i],
                getWrappedPacket().getChunksY()[i],
                getWrappedPacket().getChunksInflatedBuffers()[i],
                (short) getWrappedPacket().getChunksMask()[i],
                (short) getWrappedPacket().getChunksExtraMask()[i]
        );
    }

    @Override
    public void packDataForSending() {
        byte[] allChunks = getWrappedPacket().getUncompressedData();

        boolean orebfuscatorOn = NoXray.getInstance().getServer().getPluginManager().isPluginEnabled("Orebfuscator");

        if(orebfuscatorOn) {
            int size = 0;

            for(int i = 0; i < getAmountOfChunks(); i++) {
                size += getWrappedPacket().getChunksInflatedBuffers()[i].length;
            }

            allChunks = new byte[size];
        }

        int runningOffset = 0;
        for(int i = 0; i < getAmountOfChunks(); i++) {
            byte[] thisChunk = getWrappedPacket().getChunksInflatedBuffers()[i];
            System.arraycopy(thisChunk, 0, allChunks, runningOffset, thisChunk.length);
            runningOffset += thisChunk.length;
        }

        if(orebfuscatorOn) {
            Deflater def = new Deflater();
            def.setInput(allChunks);
            def.finish();
            int size = def.deflate(getWrappedPacket().getHandle().getByteArrays().read(0));
            getWrappedPacket().setDataLength(size);
            def.end();
        }
    }

    @Override
    public int getAmountOfChunks() {
        return getWrappedPacket().getChunksX().length;
    }

    @Override
    protected WrapperPlayServerMapChunkBulk getWrappedPacket() {
        return ((WrapperPlayServerMapChunkBulk) super.getWrappedPacket());
    }

    @Override
    protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
        return new WrapperPlayServerMapChunkBulk(packet);
    }

    @Override
    protected PacketType getAllowedPacketType() {
        return PacketType.Play.Server.MAP_CHUNK_BULK;
    }
}
