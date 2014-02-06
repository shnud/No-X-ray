package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;

/**
 * Created by Andrew on 08/01/2014.
 */
public abstract class AbstractMapChunkPacketHelper extends AbstractPacketHelper {

    public AbstractMapChunkPacketHelper(AbstractPacket packet) {
        super(packet);
    }

    public abstract MapChunkDataWrapper getMutableChunk(int i);

    public abstract int getAmountOfChunks();

    public abstract void packDataForSending();
}
