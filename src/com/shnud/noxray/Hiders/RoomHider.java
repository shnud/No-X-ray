package com.shnud.noxray.Hiders;

import com.shnud.noxray.World.MirrorWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 26/12/2013.
 */
public class RoomHider {

    private World _world;
    private MirrorWorld _mirror;

    public RoomHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _mirror = new MirrorWorld(_world);
    }

    public void saveAllData() {
        _mirror.saveAllData();
    }

    public void hideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;
    }

    public void unHideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;
    }

    public void setBlockToRoom(int x, int y, int z, int roomID) {

    }

    public void unHideBlock(int x, int y, int z) {

    }

    public void getRoomIDAtBlock(int x, int y, int z) {

    }
}
