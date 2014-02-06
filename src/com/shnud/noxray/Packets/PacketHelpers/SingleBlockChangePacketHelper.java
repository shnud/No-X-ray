package com.shnud.noxray.Packets.PacketHelpers;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrew on 15/01/2014.
 */
public class SingleBlockChangePacketHelper extends AbstractBlockChangePacketHelper {

    public SingleBlockChangePacketHelper(WrapperPlayServerBlockChange packet) {
        super(packet);
    }

    @Override
    public int getAmountOfBlockChanges() {
        return 1;
    }

    @Override
    public MapBlock getBlockChange(int i) {
        if(i != 0)
            throw new ArrayIndexOutOfBoundsException(i);

        return new MapBlock(
                getWrappedPacket().getBlockType().getId(),
                getWrappedPacket().getBlockMetadata(),
                getWrappedPacket().getX(),
                getWrappedPacket().getY(),
                getWrappedPacket().getZ()
        );
    }

    @Override
    public void setBlockChange(int i, MapBlock block) {
        if(i != 0)
            throw new ArrayIndexOutOfBoundsException(i);

        getWrappedPacket().setBlockType(Material.getMaterial(block.getBlockID()));
        getWrappedPacket().setBlockMetadata(block.getMetadata());
        getWrappedPacket().setX(block.getX());
        getWrappedPacket().setY(block.getY());
        getWrappedPacket().setZ(block.getZ());
    }

    @Override
    public void setBlockChanges(List<MapBlock> blocks) {
        if(blocks.size() > 1)
            throw new IllegalArgumentException("Single block change array can only contain 1 block change");

        setBlockChange(0, blocks.get(0));
    }

    @Override
    public LinkedList<MapBlock> getBlockChanges() {
        return new LinkedList<MapBlock>() {{
            add(getBlockChange(0));
        }};
    }

    protected WrapperPlayServerBlockChange getWrappedPacket() {
        return (WrapperPlayServerBlockChange) super.getWrappedPacket();
    }

    @Override
    protected AbstractPacket getWrappedPacketFromPacket(PacketContainer packet) {
        return new WrapperPlayServerBlockChange(packet);
    }

    @Override
    protected PacketType getAllowedPacketType() {
        return PacketType.Play.Server.BLOCK_CHANGE;
    }
}
