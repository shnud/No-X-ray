package com.shnud.noxray.Packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketAdapters.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;

/**
 * Created by Andrew on 24/12/2013.
 */
public class PacketEventListener implements Listener {
    private static PacketEventListener _instance;
    private static final NoXray _plugin = NoXray.getInstance();
    private static final ArrayList<IPacketEventWrapperListener> _listeners = new ArrayList<IPacketEventWrapperListener>();
    private final ProtocolManager _pm;

    public PacketEventListener() {
        _instance = this;
        _pm = ProtocolLibrary.getProtocolManager();

        registerPacketListeners();
        NoXray.getInstance().getServer().getPluginManager().registerEvents(_instance, _plugin);
    }

    public static PacketEventListener get() {
        if(_instance == null)
            _instance = new PacketEventListener();

        return _instance;
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent event) {
        if(event.getPlugin() == _plugin) {
            _pm.removePacketListeners(_plugin);
            _pm.getAsynchronousManager().cleanupAll();
        }
    }

    public void addListener(IPacketEventWrapperListener listener) {
        if(listener == null)
            throw new IllegalArgumentException("Listener cannot be null");

        if(!_listeners.contains(listener))
            _listeners.add(listener);
    }

    public void removeListener(IPacketEventWrapperListener listener) {
        if(listener == null)
            return;

        _listeners.remove(listener);
    }

    private void registerPacketListeners() {
        _pm.addPacketListener(new NamedEntitySpawnAdapter(_listeners));
        _pm.addPacketListener(new EntityDestroyAdapter(_listeners));
        _pm.addPacketListener(new EntitySpawnAdapter(_listeners));
        _pm.addPacketListener(new SingleEntityUpdateAdapter(_listeners));
        _pm.addPacketListener(new MultipleEntityUpdateAdapter(_listeners));

        _pm.getAsynchronousManager().registerAsyncHandler(new MapChunkAdapter(_listeners)).start();
        _pm.getAsynchronousManager().registerAsyncHandler(new BlockChangeAdapter(_listeners)).start();
    }
}
