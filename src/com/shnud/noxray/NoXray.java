package com.shnud.noxray;

import com.shnud.noxray.Commands.CommandListener;
import com.shnud.noxray.EntityHiding.EntityHider;
import com.shnud.noxray.EntityHiding.PlayerHider;
import com.shnud.noxray.RoomHiding.RoomHider;
import com.shnud.noxray.Settings.NoXraySettings;
import com.shnud.noxray.Settings.PlayerMetadataEntry;
import com.shnud.noxray.Settings.PlayerMetadataStore;
import org.bukkit.ChatColor;
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
    private static final ArrayList<PlayerHider> _playerHiders = new ArrayList<PlayerHider>();
    private static final ArrayList<EntityHider> _entityHiders = new ArrayList<EntityHider>();
    private static final ArrayList<RoomHider> _roomHiders = new ArrayList<RoomHider>();
    private static CommandListener _commandListener;
    private static PlayerMetadataStore _metadataStore;

    public NoXray() {
        _instance = this;
    }

    public static NoXray getInstance() {

        return _instance;
    }

    public static void log(Level level, String message) {
        getInstance().getServer().getLogger().log(level, message);
    }

    public static void broadcast(String message) {
        getInstance().getServer().broadcastMessage(ChatColor.YELLOW + "[No X-ray] " + message);
    }

    @Override
    public void onEnable() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        NoXraySettings.initSettings();

        loadPlayerHiders();
        loadEntityHiders();
        loadRoomHiders();

        _commandListener = new CommandListener();
        _metadataStore = new PlayerMetadataStore();
        getCommand("hide").setExecutor(_commandListener);
        getCommand("unhide").setExecutor(_commandListener);
        getCommand("auto").setExecutor(_commandListener);
        getCommand("status").setExecutor(_commandListener);
    }

    @Override
    public void onDisable() {
        for(RoomHider rh : _roomHiders) rh.disable();
        _metadataStore.save();
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

    public static PlayerMetadataEntry getPlayerMetadata(String name) {
        return _metadataStore.getMetadataForPlayer(name);
    }

    public static PlayerMetadataEntry getPlayerMetadata(Player player) {
        return getPlayerMetadata(player.getName());
    }

    public RoomHider getRoomHider(World world) {
        for(RoomHider hider : _roomHiders)
            if(hider.getWorld().equals(world)) return hider;

        return null;
    }
}
