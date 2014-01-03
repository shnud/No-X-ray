package com.shnud.noxray;

import com.shnud.noxray.Hiders.EntityHider;
import com.shnud.noxray.Hiders.PlayerHider;
import com.shnud.noxray.Hiders.RoomHider;
import com.shnud.noxray.Settings.NoXraySettings;
import com.shnud.noxray.Settings.PlayerMetadata;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NoXray extends JavaPlugin {

    private static NoXray _instance;

    public NoXray() {
        if(_instance == null)
            _instance = this;
        else
            throw new IllegalStateException("Cannot have two base objects open");
    }

    public static NoXray getInstance() {
        return _instance;
    }

    private ArrayList<PlayerHider> _playerHiders = new ArrayList<PlayerHider>();
    private ArrayList<EntityHider> _entityHiders = new ArrayList<EntityHider>();
    private ArrayList<RoomHider> _roomHiders = new ArrayList<RoomHider>();
    private PlayerMetadata _playerMetadata = new PlayerMetadata();

    @Override
    public void onEnable() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        NoXraySettings.initSettings();
    }

    @Override
    public void onDisable() {
        for(PlayerHider ph : _playerHiders) {
            ph.deactivate();
        }

        for(RoomHider rh : _roomHiders) {
            rh.deactivate();
        }

        _playerHiders.clear();
        _entityHiders.clear();
        _roomHiders.clear();
        _playerMetadata.save();
        getServer().getScheduler().cancelTasks(this);
    }

    public void loadPlayerHiders() {
        for (String worldName : NoXraySettings.getPlayerHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _playerHiders.add(new PlayerHider(world));
            else
                Bukkit.getLogger().log(Level.WARNING, "Could not load player hider for world \"" + worldName);
        }
    }

    public void loadEntityHiders() {
        for (String worldName : NoXraySettings.getEntityHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _entityHiders.add(new EntityHider(world));
            else
                Bukkit.getLogger().log(Level.WARNING, "Could not load entity hider for world \"" + worldName);
        }
    }

    public void loadRoomHiders() {
        for (String worldName : NoXraySettings.getRoomHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _roomHiders.add(new RoomHider(world));
            else
                Bukkit.getLogger().log(Level.WARNING, "Could not load room hider for world \"" + worldName);
        }
    }
}
