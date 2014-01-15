package com.shnud.noxray.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import com.shnud.noxray.World.MapBlock;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrew on 15/01/2014.
 */
public class SingleBlockChangePacketEvent extends BlockChangePacketEvent {
    public SingleBlockChangePacketEvent(Player receiver, PacketEvent event) {
        super(receiver, event);
        if(event.getPacketType() != PacketType.Play.Server.BLOCK_CHANGE)
            throw new IllegalArgumentException("Event must have packet of type BLOCK_CHANGE");
    }

    @Override
    public int getNumberOfBlocks() {
        return 1;
    }

    @Override
    public MapBlock getBlock(int i) {
        if(i != 0)
            throw new IllegalArgumentException("Single block change packets only have 1 block");

        final int x = getPacket().getIntegers().read(0);
        final int y = getPacket().getIntegers().read(1);
        final int z = getPacket().getIntegers().read(2);
        final Material mat = getPacket().getBlocks().read(0);
        final int ID = mat.getId();
        final byte metadata = getPacket().getIntegers().read(3).byteValue();

        return new MapBlock(ID, metadata, x, y, z);
    }

    @Override
    public void setBlock(int i, MapBlock block) {
        if(i != 0)
            throw new IllegalArgumentException("Single block change packets only have 1 block");

        if(block == null)
            throw new IllegalArgumentException("Block cannot be null");

        getPacket().getIntegers().write(0, block.getX()); // x coordinate of block
        getPacket().getIntegers().write(1, block.getY()); // y coordinate of block
        getPacket().getIntegers().write(2, block.getZ()); // z coordinate of block
        Material mat = Material.getMaterial(block.getBlockID());

        if(mat == null)
            throw new IllegalArgumentException("Block ID/material was not valid");

        getPacket().getBlocks().write(0, mat); // block ID
        getPacket().getIntegers().write(3, (int) block.getMetadata()); // block metadata
    }

    @Override
    public void setBlocks(List<MapBlock> blocks) {
        if(blocks == null)
            throw new IllegalArgumentException("Blocks cannot be null");

        if(blocks.size() > 1)
            throw new IllegalArgumentException("Blocks in single block change packet must not be greater than 1");

        setBlock(0, blocks.get(0));
    }

    @Override
    public LinkedList<MapBlock> getBlocks() {
        LinkedList list = new LinkedList();
        list.add(getBlock(0));
        return list;
    }
}
