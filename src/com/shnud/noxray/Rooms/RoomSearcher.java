package com.shnud.noxray.Rooms;

import com.shnud.noxray.Utilities.XYZ;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Andrew on 22/12/2013.
 */
public class RoomSearcher {
    private int _originX, _originY, _originZ;
    private Material[] _wallBlocks = new Material[]{Material.GRAVEL, Material.STONE, Material.DIRT, Material.IRON_ORE, Material.COAL_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.REDSTONE_ORE, Material.EMERALD_ORE};
    private World _world;
    private static final int MAX_BLOCK_SEARCH = 100000;

    public RoomSearcher(int x, int y, int z, World world) {
        _originX = x;
        _originY = y;
        _originZ = z;
        _world = world;
    }

    public void setWallBlocks(Material[] materials) {
        if(materials.length == 0)
            throw new IllegalArgumentException("Search will go on forever with no wall blocks");

        _wallBlocks = materials;
    }

    public ArrayList<XYZ> getRoomBlocks() throws ChunkNotLoadedException, MaxBlocksReachedException {
        ArrayList<XYZ> valid = new ArrayList<XYZ>();
        HashSet<XYZ> opened = new HashSet<XYZ>(); // List of all those that have been checked to prevent repeats
        LinkedList<XYZ> open = new LinkedList<XYZ>();

        XYZ initialBlock = new XYZ(_originX, _originY, _originZ);
        opened.add(initialBlock);
        open.add(initialBlock);

        int count = 0;

        while(!open.isEmpty() && count < MAX_BLOCK_SEARCH)
        {
            XYZ curr = open.remove();

            if(!isChunkLoadedForBlock(curr))
                throw new ChunkNotLoadedException();

            if(!isBlockValid(curr))
                continue;

            valid.add(curr);

            XYZ[] toCheck = new XYZ[]{
                    new XYZ(curr.x + 1, curr.y, curr.z),
                    new XYZ(curr.x, curr.y + 1, curr.z),
                    new XYZ(curr.x, curr.y, curr.z + 1),
                    new XYZ(curr.x - 1, curr.y, curr.z),
                    new XYZ(curr.x, curr.y - 1, curr.z),
                    new XYZ(curr.x, curr.y, curr.z - 1)
            };

            for(XYZ c : toCheck) {
                if(opened.contains(c))
                    continue;

                opened.add(c);
                open.add(c);
            }

            count++;
        }

        if(count >= MAX_BLOCK_SEARCH)
            throw new MaxBlocksReachedException();

        return valid;
    }

    private boolean isBlockValid(XYZ block) {
        Material type = _world.getBlockAt(block.x, block.y, block.z).getType();

        for(Material wall : _wallBlocks) {
            if(type == wall)
                return false;
        }

        return true;
    }

    private boolean isChunkLoadedForBlock(XYZ block) {
        return _world.isChunkLoaded(block.x / 16, block.y / 16);
    }

    public class ChunkNotLoadedException extends Exception {}
    public class MaxBlocksReachedException extends Exception {}
}
