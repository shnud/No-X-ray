package com.shnud.noxray.Packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.Events.*;
import com.shnud.noxray.NoXray;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

/**
 * Created by Andrew on 24/12/2013.
 */
public class PacketListener {
    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();
    private static NoXray _plugin = NoXray.getInstance();
    private static PacketListener _instance;
    private static ArrayList<PacketEventListener> _listeners = new ArrayList<PacketEventListener>();
    private static boolean hasInitialised = init();

    private static boolean init() {
        registerPacketListeners();

        return true;
    }

    private static void dispatchEventToListeners(BasePacketEvent event) {
        for (PacketEventListener listener : _listeners) {
            listener.receivePacketEvent(event);
        }
    }

    public static void addEventListener(PacketEventListener listener) {
        if(listener == null)
            throw new IllegalArgumentException("Listener cannot be null");

        _listeners.add(listener);
    }

    public static void removeEventListener(PacketEventListener listener) {
        if(listener == null)
            return;

        _listeners.remove(listener);
    }

    private static void registerPacketListeners() {
        _pm.addPacketListener(new NamedEntitySpawnAdapter());
        _pm.addPacketListener(new EntitySpawnAdapter());
        _pm.addPacketListener(new EntityUpdateAdapter());
    }

    private static class NamedEntitySpawnAdapter extends PacketAdapter {

        public NamedEntitySpawnAdapter() {
            super(_plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if(event.isCancelled())
                return;

            PacketContainer packet = event.getPacket();
            World world = event.getPlayer().getWorld();

            int subjectID = packet.getIntegers().read(0);
            Entity subject = _pm.getEntityFromID(world, subjectID);

            PlayerSpawnPacketEvent entityEvent = new PlayerSpawnPacketEvent(event.getPlayer(), subject, event);
            dispatchEventToListeners(entityEvent);
        }
    }

    private static class EntitySpawnAdapter extends PacketAdapter {
        public EntitySpawnAdapter() {
            super(_plugin, ListenerPriority.HIGHEST, PacketUtils.getAllEntitySpawnPackets());
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if(event.isCancelled())
                return;

            PacketContainer packet = event.getPacket();
            World world = event.getPlayer().getWorld();

            int subjectID = packet.getIntegers().read(0);
            Entity subject = _pm.getEntityFromID(world, subjectID);

            EntitySpawnPacketEvent entityEvent = new EntitySpawnPacketEvent(event.getPlayer(), subject, event);
            dispatchEventToListeners(entityEvent);
        }
    }

    private static class EntityUpdateAdapter extends PacketAdapter {

        public EntityUpdateAdapter() {
            super(_plugin, ListenerPriority.HIGHEST, PacketUtils.getAllEntityUpdatePackets());
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if(event.isCancelled())
                return;

            PacketContainer packet = event.getPacket();
            World world = event.getPlayer().getWorld();

            int subjectID = PacketUtils.getEntityIDFromEntityPacket(packet);
            Entity subject = _pm.getEntityFromID(world, subjectID);
            EntityUpdatePacketEvent entityEvent = new EntityUpdatePacketEvent(event.getPlayer(), subject, event);
            dispatchEventToListeners(entityEvent);
        }
    }
}
