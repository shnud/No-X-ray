package com.shnud.noxray.Entities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.shnud.noxray.Structures.IterableHashMap;
import com.shnud.noxray.Structures.SyncIterableHashMap;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityWatcherEntry {

    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();
    private SyncIterableHashMap<String, Player> _watchers = new SyncIterableHashMap<String, Player>();
    private Entity _subject;
    private World _initialWorld;

    public EntityWatcherEntry(Entity subject) {
        _subject = subject;
        _initialWorld = subject.getWorld();
    }

    public EntityWatcherEntry(Entity subject, Player firstwatcher) {
        this(subject);
        _watchers.put(firstwatcher.getName(), firstwatcher);
    }

    public void addWatcher(Player player) {
        if(hasWatcher(player))
            return;

        _watchers.put(player.getName(), player);
    }

    public void removeWatcher(Player player) {
        if(hasWatcher(player))
            _watchers.remove(player.getName());
    }

    /*
     * Uses protocolLib to find the players that should
     * actually be watching the entity for events,
     * and removes any watchers than are not in this and
     * are thus, watching unnecessarily
     */
    public void purgeWatchers() {
        SyncIterableHashMap<String, Player> _leftoverWatchers = new SyncIterableHashMap<String, Player>();

        try {
            List<Player> realWatchers = _pm.getEntityTrackers(_subject);

            for (Player p : realWatchers) {
                if(hasWatcher(p))
                    _leftoverWatchers.put(p.getName(), p);
            }
        } catch (IllegalArgumentException e) {
            /* Some bug with ProtocolLib throws an exception when there are no watchers,
             * so we have to catch it here instead of testing for a simple empty list
             */
        }

        _watchers = _leftoverWatchers;
    }

    public boolean hasWatcher(Player player) {
        return _watchers.containsKey(player.getName());
    }

    public int numberOfWatchers() {
        return _watchers.size();
    }

    public boolean hasChangedWorlds() {
        return !_subject.getWorld().equals(_initialWorld);
    }

    public boolean isDead() {
        return _subject.isDead();
    }

    public Entity getEntity() {
        return _subject;
    }

    public Iterator<Player> getWatcherIterator() {
        return _watchers.iterator();
    }
}
