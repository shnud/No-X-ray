package com.shnud.noxray.Entities;

import com.shnud.noxray.Structures.IterableHashMap;
import com.shnud.noxray.Structures.SyncIterableHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityWatcherList implements Iterable<EntityWatcherEntry> {

    SyncIterableHashMap<Integer, EntityWatcherEntry> _entities = new SyncIterableHashMap<Integer, EntityWatcherEntry>();

    public void addWatcherToEntity(Player newWatcher, Entity subject) {
        if(containsEntity(subject))
            _entities.get(subject.getEntityId()).addWatcher(newWatcher);
        else
            _entities.put(subject.getEntityId(), new EntityWatcherEntry(subject, newWatcher));
    }

    public boolean containsEntity(Entity entity) {
        return _entities.containsKey(entity.getEntityId());
    }

    public EntityWatcherEntry getEntryForEntity(Entity entity) {
        return _entities.get(entity.getEntityId());
    }

    public void removeEntityEntry(Entity entity) {
        _entities.remove(entity.getEntityId());
    }

    public void removeWatcherFromEntity(Player watcher, Entity entity) {
        if(containsEntity(entity)) {
            EntityWatcherEntry entry = getEntryForEntity(entity);
            entry.removeWatcher(watcher);

            if(entry.numberOfWatchers() == 0)
                removeEntityEntry(entity);
        }
    }

    public boolean doesEntityHaveWatcher(Entity entity, Player watcher) {
        if(!containsEntity(entity))
            return false;

        return getEntryForEntity(entity).hasWatcher(watcher);
    }

    /*
     * Removes any entities from the list which have no real
     * watchers according to protocolLib, or are dead, or have
     * transported into a different world
     */
    public void purgeList() {
        Iterator it = _entities.iterator();

        while(it.hasNext()) {
            EntityWatcherEntry ew = (EntityWatcherEntry) it.next();
            if(ew.isDead() || ew.hasChangedWorlds()) {
                it.remove();
                continue;
            }

            ew.purgeWatchers();
            if(ew.numberOfWatchers() == 0)
                it.remove();
        }
    }

    public int size() {
        return _entities.size();
    }

    @Override
    public Iterator<EntityWatcherEntry> iterator() {
        return _entities.iterator();
    }
}
