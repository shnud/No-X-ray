package com.shnud.noxray;

import com.shnud.noxray.Hiders.EntityHider;
import com.shnud.noxray.Hiders.PlayerHider;
import com.shnud.noxray.Hiders.RoomHider;
import com.shnud.noxray.Structures.DynamicByteBitWrapper;
import com.shnud.noxray.Structures.NibbleArray;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

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

    @Override
    public void onLoad() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();
    }

    @Override
    public void onEnable() {
        _playerHiders.add(new PlayerHider(getServer().getWorld("world"), true));
        _entityHiders.add(new EntityHider(getServer().getWorld("world")));
        _roomHiders.add(new RoomHider(getServer().getWorld("world")));
    }

    @Override
    public void onDisable() {
        for(PlayerHider ph : _playerHiders) {
            ph.deactivate();
        }

        _playerHiders.clear();
        _entityHiders.clear();
        _roomHiders.clear();
        getServer().getScheduler().cancelTasks(this);
    }
}
