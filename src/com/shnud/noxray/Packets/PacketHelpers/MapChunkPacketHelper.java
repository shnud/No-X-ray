package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.zip.Deflater;

public class MapChunkPacketHelper extends AbstractMapChunkPacketHelper {

    public MapChunkPacketHelper(WrapperPlayServerMapChunk packet) {
        super(packet);
    }

    @Override
    public MapChunkDataWrapper getMutableChunk(int i) {
        if(i != 0)
            throw new ArrayIndexOutOfBoundsException(i);

        return new MapChunkDataWrapper(
                getWrappedPacket().getChunkX(),
                getWrappedPacket().getChunkZ(),
                getWrappedPacket().getUncompressedData(),
                getWrappedPacket().getPrimaryBitMap(),
                getWrappedPacket().getAddBitMap()
        );
    }

    @Override
    public void packDataForSending() {
        Deflater def = new Deflater();
        def.setInput(getWrappedPacket().getUncompressedData());
        def.finish();
        int deflatedLength = def.deflate(getWrappedPacket().getCompressedData());
        getWrappedPacket().setCompressedSize(deflatedLength);
        def.end();
    }

    @Override
    public int getAmountOfChunks() {
        return 1;
    }

    @Override
    protected WrapperPlayServerMapChunk getWrappedPacket() {
        return ((WrapperPlayServerMapChunk) super.getWrappedPacket());
    }

    @Override
    protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
        return new WrapperPlayServerMapChunk(packet);
    }

    @Override
    protected PacketType getAllowedPacketType() {
        return PacketType.Play.Server.MAP_CHUNK;
    }
}
