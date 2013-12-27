package com.shnud.noxray.Rooms;

import com.shnud.noxray.Settings.NoXraySettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorManager implements Listener {

    private static MirrorManager _instance;
    private NoXraySettings _NoXray_settings;
    private HashMap<String, MirrorRegion> _regions;

    public static MirrorManager getMirrorManager() {
        if(_instance == null)
            _instance = new MirrorManager();

        return _instance;
    }


}
