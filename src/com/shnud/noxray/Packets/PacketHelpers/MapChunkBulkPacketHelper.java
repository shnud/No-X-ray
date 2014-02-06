package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunkBulk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

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

        int runningOffset = 0;
        for(int i = 0; i < getAmountOfChunks(); i++) {
            byte[] thisChunk = getWrappedPacket().getChunksInflatedBuffers()[i];
            System.arraycopy(thisChunk, 0, allChunks, runningOffset, thisChunk.length);
            runningOffset += thisChunk.length;
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
