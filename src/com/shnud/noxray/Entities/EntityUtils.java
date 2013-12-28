package com.shnud.noxray.Entities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Andrew on 28/12/2013.
 */
public class EntityUtils {

    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();

    public static boolean isEntityBeingTrackedBy(Entity target, Player client) {
        List<Player> realWatchers = _pm.getEntityTrackers(target);

        if(realWatchers.contains(client))
            return true;

        return false;
    }
}
