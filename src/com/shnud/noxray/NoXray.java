package com.shnud.noxray;

import com.shnud.noxray.Entities.EntityHider;
import com.shnud.noxray.Entities.PlayerHider;
import com.shnud.noxray.Rooms.RoomHider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NoXray extends JavaPlugin {

    private static NoXray _instance;
    private static long _mainThreadId = Thread.currentThread().getId();

    public NoXray() {
        if(_instance == null)
            _instance = this;
        else
            throw new IllegalStateException("Cannot have two base objects open");
    }

    public static NoXray getInstance() {
        return _instance;
    }

    private ArrayList<PlayerHider> _playerHiders;
    private RoomHider _roomHider;

    @Override
    public void onEnable() {
        PlayerHider ph = new PlayerHider(getServer().getWorld("world"));
        EntityHider eh = new EntityHider(getServer().getWorld("world"));
    }

    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    public boolean isMainThread(Thread t) {
        return t.getId() == _mainThreadId;
    }
}
