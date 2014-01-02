package com.shnud.noxray.Hiders;

import com.shnud.noxray.World.MirrorWorld;
import org.bukkit.World;

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
}
