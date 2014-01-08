package com.shnud.noxray;

import com.shnud.noxray.Commands.CommandListener;
import com.shnud.noxray.Hiders.EntityHider;
import com.shnud.noxray.Hiders.PlayerHider;
import com.shnud.noxray.Hiders.RoomHider;
import com.shnud.noxray.Settings.NoXraySettings;
import com.shnud.noxray.Settings.PlayerMetadataBank;
import com.shnud.noxray.Settings.PlayerMetadataEntry;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NoXray extends JavaPlugin {

    private static NoXray _instance;

    public NoXray() {
        _instance = this;
    }

    public static NoXray getInstance() {
        return _instance;
    }

    private final ArrayList<PlayerHider> _playerHiders = new ArrayList<PlayerHider>();
    private final ArrayList<EntityHider> _entityHiders = new ArrayList<EntityHider>();
    private final ArrayList<RoomHider> _roomHiders = new ArrayList<RoomHider>();
    private final PlayerMetadataBank _metadataBank = new PlayerMetadataBank();
    private final CommandListener _commandListener = new CommandListener();

    @Override
    public void onEnable() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        NoXraySettings.initSettings();

        loadPlayerHiders();
        loadEntityHiders();
        loadRoomHiders();

        getCommand("hide").setExecutor(_commandListener);
        getCommand("unhide").setExecutor(_commandListener);
        getCommand("auto").setExecutor(_commandListener);
        getCommand("status").setExecutor(_commandListener);
    }

    @Override
    public void onDisable() {
        for(RoomHider rh : _roomHiders) rh.disable();
        _metadataBank.save();
        getServer().getScheduler().cancelTasks(this);
    }

    private void loadPlayerHiders() {
        for (String worldName : NoXraySettings.getPlayerHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _playerHiders.add(new PlayerHider(world));
            else
                NoXray.getInstance().getLogger().log(Level.WARNING, "Could not load player hider for world \"" + worldName + "\"");
        }
    }

    private void loadEntityHiders() {
        for (String worldName : NoXraySettings.getEntityHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _entityHiders.add(new EntityHider(world));
            else
                NoXray.getInstance().getLogger().log(Level.WARNING, "Could not load entity hider for world \"" + worldName + "\"");
        }
    }

    private void loadRoomHiders() {
        for (String worldName : NoXraySettings.getRoomHideWorlds()) {
            World world = getServer().getWorld(worldName);
            if(world != null)
                _roomHiders.add(new RoomHider(world));
            else
                NoXray.getInstance().getLogger().log(Level.WARNING, "Could not load room hider for world \"" + worldName + "\"");
        }
    }

    public RoomHider getRoomHider(World world) {
        for(RoomHider hider : _roomHiders) {
            if(hider.getWorld().equals(world))
                return hider;
        }

        return null;
    }

    public PlayerMetadataEntry getPlayerMetadata(String name) {
        return _metadataBank.getMetadataForPlayer(name);
    }

    public PlayerMetadataEntry getPlayerMetadata(Player player) {
        return getPlayerMetadata(player.getName());
    }
}
