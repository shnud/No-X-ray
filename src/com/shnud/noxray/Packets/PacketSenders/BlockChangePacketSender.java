package com.shnud.noxray.Packets.PacketSenders;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.google.common.collect.Lists;
import com.shnud.noxray.World.MapBlock;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Andrew on 09/01/2014.
 */
public class BlockChangePacketSender extends AbstractPacketSender {
    private static final int MAX_BLOCKS_PER_PACKET = 64;
    private final int _chunkX;
    private final int _chunkZ;
    private final List<MapBlock> _localBlocks;

    public BlockChangePacketSender(List<Player> receivers, int chunkX, int chunkZ, List<MapBlock> localBlocks) {
        super(receivers);
        if(localBlocks == null)
            throw new IllegalArgumentException("Blocks cannot be null");

        _chunkX = chunkX;
        _chunkZ = chunkZ;
        _localBlocks = localBlocks;
    }

    public BlockChangePacketSender(Player receiver, int chunkX, int chunkZ, List<MapBlock> localBlocks) {
        this(Lists.newArrayList(receiver), chunkX, chunkZ, localBlocks);
    }

    @Override
    protected boolean isThreadSafe() {
        return true;
    }

    @Override
    protected void sendImplementation() {

        int sections = _localBlocks.size() / MAX_BLOCKS_PER_PACKET;
        if(_localBlocks.size() % MAX_BLOCKS_PER_PACKET != 0) sections++;

        // We will split the packet into multiple if necessary so as not to send
        // one huge packet full of hundreds/thousands of blocks
        for(int section = 0; section < sections; section++) {

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
            packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(_chunkX, _chunkZ));
            // The data is 4 * the size of record count, 4 bytes per record
            int records;
            if(section == sections - 1)
                records = _localBlocks.size() % MAX_BLOCKS_PER_PACKET;
            else
                records = MAX_BLOCKS_PER_PACKET;

            byte[] data = new byte[records * 4];

            int start = section * MAX_BLOCKS_PER_PACKET;
            int i;
            for(i = start; i < start + MAX_BLOCKS_PER_PACKET && i < _localBlocks.size(); i++) {
                MapBlock block = _localBlocks.get(i);
                int dataIndex = (i-start) * 4;

                // F0 00 00 00 <- x
                data[dataIndex + 0] |= block.getX() << 4;
                // 0F 00 00 00 <- z
                data[dataIndex + 0] |= block.getZ();
                // 00 FF 00 00 <- y
                data[dataIndex + 1] |= block.getY();
                // 00 00 FF F0 <- block ID
                data[dataIndex + 2] |= block.getBlockID() >> 4;
                data[dataIndex + 3] |= block.getBlockID() << 4;
                // 00 00 00 0F <- block metadata
                data[dataIndex + 3] |= block.getMetadata();
            }

            // Set the data
            packet.getByteArrays().write(0, data);

            // Set the record count
            packet.getIntegers().write(0, records);

            for(Player p : _receivers) {
                try {
                    getProtocolManager().sendServerPacket(p, packet, false);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
