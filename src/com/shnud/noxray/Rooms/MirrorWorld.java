package com.shnud.noxray.Rooms;

import com.shnud.noxray.NoXray;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;

/**
 * Created by Andrew on 28/12/2013.
 */
public class MirrorWorld implements Listener {

    private HashMap<String, MirrorRegion> _regionMap = new HashMap<String, MirrorRegion>();
    private World _world;

    public MirrorWorld(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        Bukkit.getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    public boolean containsRegion(int x, int z) {
        return _regionMap.containsKey(keyFromCoordinates(x, z));
    }

    private static String keyFromCoordinates(int x, int z) {
        return x + ":" + z;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event) {
        if(!event.getWorld().equals(_world))
            return;
    }
}
