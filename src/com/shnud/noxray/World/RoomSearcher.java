package com.shnud.noxray.World;

import com.shnud.noxray.Utilities.MagicValues;
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
    private final int _originX, _originY, _originZ;
    private final HashSet<Material> _wallBlocks = new HashSet<Material>();
    private final World _world;
    private static final int MAX_BLOCK_SEARCH = 100000;

    public RoomSearcher(int x, int y, int z, World world) {
        _originX = x;
        _originY = y;
        _originZ = z;
        _world = world;

        if(world.getEnvironment() == World.Environment.NORMAL) {
            _wallBlocks.add(Material.GRAVEL);
            _wallBlocks.add(Material.STONE);
            _wallBlocks.add(Material.DIRT);
            _wallBlocks.add(Material.IRON_ORE);
            _wallBlocks.add(Material.COAL_ORE);
            _wallBlocks.add(Material.GOLD_ORE);
            _wallBlocks.add(Material.DIAMOND_ORE);
            _wallBlocks.add(Material.REDSTONE_ORE);
            _wallBlocks.add(Material.EMERALD_ORE);
        }
        else if(world.getEnvironment() == World.Environment.NETHER) {
            _wallBlocks.add(Material.NETHERRACK);
        }
        else if(world.getEnvironment() == World.Environment.THE_END) {
            _wallBlocks.add(Material.ENDER_STONE);
        }
    }

    public ArrayList<XYZ> searchAndReturnRoomBlocks() throws ChunkNotLoadedException, MaxBlocksReachedException {
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

        if(_wallBlocks.contains(type))
            return false;
        else
            return true;
    }

    private boolean isChunkLoadedForBlock(XYZ block) {
        return _world.isChunkLoaded(block.x >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, block.z >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK);
    }

    public class ChunkNotLoadedException extends Exception {}
    public class MaxBlocksReachedException extends Exception {}
}
