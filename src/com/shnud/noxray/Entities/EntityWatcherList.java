package com.shnud.noxray.Entities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.shnud.noxray.Structures.HashMapArrayList;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityWatcherList implements Iterable<Player> {

    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();
    private HashMapArrayList<String, Player> _watchers = new HashMapArrayList<String, Player>();
    private Entity _subject;
    private World _initialWorld;

    public EntityWatcherList(Entity subject) {
        _subject = subject;
        _initialWorld = subject.getWorld();
    }

    public EntityWatcherList(Entity subject, Player firstwatcher) {
        this(subject);
        _watchers.put(firstwatcher.getName(), firstwatcher);
    }

    public void addWatcher(Player player) {
        if(isWatching(player))
            return;

        _watchers.put(player.getName(), player);
    }

    public void removeWatcher(Player player) {
        if(isWatching(player))
            _watchers.remove(player.getName());
    }

    /*
     * Uses protocolLib to find the players that should
     * actually be watching the entity for events,
     * and removes any watchers than are not in this and
     * are thus, watching unnecessarily
     */
    public void purgeWatchers() {
        HashMapArrayList<String, Player> _leftoverWatchers = new HashMapArrayList<String, Player>();

        try {
            List<Player> realWatchers = _pm.getEntityTrackers(_subject);

            for (Player p : realWatchers) {
                if(isWatching(p))
                    _leftoverWatchers.put(p.getName(), p);
            }
        } catch (IllegalArgumentException e) {
            /* Some bug with ProtocolLib throws an exception when there are no watchers,
             * so we have to catch it here instead of testing for a simple empty list
             */
        }

        _watchers = _leftoverWatchers;
    }

    public boolean isWatching(Player player) {
        return _watchers.containsKey(player.getName());
    }

    public int numberOfWatchers() {
        return _watchers.size();
    }

    public boolean hasChangedWorlds() {
        return !_subject.getWorld().equals(_initialWorld);
    }

    public boolean subjectIsDead() {
        return _subject.isDead();
    }

    public Entity getSubject() {
        return _subject;
    }

    @Override
    public Iterator<Player> iterator() {
        return _watchers.iterator();
    }
}
