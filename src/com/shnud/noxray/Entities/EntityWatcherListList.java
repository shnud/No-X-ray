package com.shnud.noxray.Entities;

import com.shnud.noxray.Structures.HashMapArrayList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityWatcherListList implements Iterable<EntityWatcherList> {

    HashMapArrayList<Integer, EntityWatcherList> _entityWatchers = new HashMapArrayList<Integer, EntityWatcherList>();

    public void addWatcher(Entity subject, Player newWatcher) {
        if(containsEntityWatcher(subject))
            _entityWatchers.get(subject.getEntityId()).addWatcher(newWatcher);
        else
            _entityWatchers.add(subject.getEntityId(), new EntityWatcherList(subject, newWatcher));
    }

    public boolean containsEntityWatcher(Entity entity) {
        return _entityWatchers.containsKey(entity.getEntityId());
    }

    public boolean isEntityBeingHiddenFrom(Entity entity, Player watcher) {
        if(!_entityWatchers.containsKey(entity.getEntityId()))
            return false;

        return _entityWatchers.get(entity.getEntityId()).isWatching(watcher);
    }

    /*
     * Removes any entities from the list which have no real
     * watchers according to protocolLib, or are dead, or have
     * transported into a different world
     */
    public void purgeList() {
        Iterator it = _entityWatchers.iterator();

        while(it.hasNext()) {
            EntityWatcherList ew = (EntityWatcherList) it.next();
            if(ew.subjectIsDead() || ew.hasChangedWorlds()) {
                it.remove();
                continue;
            }

            ew.purgeWatchers();
            if(ew.numberOfWatchers() == 0)
                it.remove();
        }
    }

    @Override
    public Iterator iterator() {
        return _entityWatchers.iterator();
    }
}
