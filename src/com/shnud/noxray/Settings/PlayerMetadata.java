package com.shnud.noxray.Settings;

import com.shnud.noxray.Structures.IterableHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Andrew on 03/01/2014.
 */
public class PlayerMetadata implements Listener {

    private IterableHashMap<String, PlayerMetadataEntry> _players = new IterableHashMap<String, PlayerMetadataEntry>();

    public PlayerMetadata() {

    }

    private void load() {
        /*if(!_file.exists())
            return;

        _players.clear();

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(_file);

            Set<String> players = config.getKeys(false);

            for(String player : players) {
                PlayerMetadataEntry metadata = new PlayerMetadataEntry(player);
                boolean autoprotect = config.getBoolean(player + "autoprotect");
                metadata.setAutoProtect(autoprotect);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } */
    }

    public void save() {
       /* try {
            YamlConfiguration config = new YamlConfiguration();

            for(PlayerMetadataEntry metadata : _players) {

            }

            config.save(_file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to save player settings");
        }   */
    }

    public PlayerMetadataEntry getMetadataForPlayer(String name) {
        if(!_players.containsKey(name))
            _players.put(name, new PlayerMetadataEntry(name));

        return _players.get(name);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {

    }
}
