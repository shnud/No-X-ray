package com.shnud.noxray.Settings;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Structures.IterableHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Created by Andrew on 03/01/2014.
 */
@ThreadSafe
public class PlayerMetadataBank implements Listener {

    private IterableHashMap<String, PlayerMetadataEntry> _players = new IterableHashMap<String, PlayerMetadataEntry>();

    public PlayerMetadataBank() {
        NoXray.getInstance().getServer().getPluginManager().registerEvents(this, NoXray.getInstance());
    }

    private void load() {
        // todo
    }

    public void save() {
        // todo
    }

    synchronized public PlayerMetadataEntry getMetadataForPlayer(String name) {
        if(!_players.containsKey(name))
            _players.put(name, new PlayerMetadataEntry(name));

        return _players.get(name);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // todo logic to load player metadata
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // todo logic to save player metadata
    }
}
