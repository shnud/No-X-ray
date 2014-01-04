package com.shnud.noxray.Entities;

import com.shnud.noxray.Structures.IterableHashMap;
import org.bukkit.entity.Entity;

import java.util.*;

/**
 * Created by Andrew on 26/12/2013.
 */
public class PlayerCoupleList<T extends PlayerCouple> implements Iterable<T> {

    /*
     * Thread-safe; uses synchronized HashMap/Array
     */
    private IterableHashMap<Long, T> _couples = new IterableHashMap<Long, T>();

    public void addCouples(T[] couples) {
        for (T couple : couples) {
            addCouple(couple);
        }
    }

    public void addCouples(Collection<T> couples) {
        for (T couple : couples) {
            addCouple(couple);
        }
    }

    /*
     * Will not put a couple that already exists, every entry must be unique
     */
    public void addCouple(T couple) {
        if(couple == null)
            throw new IllegalArgumentException("Couple cannot be null");

        if(!containsCouple(couple))
            _couples.put(couple.uniqueID(), couple);
    }

    public void removeCouple(PlayerCouple couple) {
        if(couple == null)
            throw new IllegalArgumentException("Couple cannot be null");

        _couples.remove(couple.uniqueID());
    }

    public T getCoupleFromID(long ID) {
        if(!_couples.containsKey(ID))
            return null;

        return _couples.get(ID);
    }

    public T getCoupleFromEntities(Entity e1, Entity e2) {
        if(e1 == null || e2 == null)
            throw new IllegalArgumentException("Entities cannot be null");

        long ID = PlayerCouple.uniqueIDFromEntityPair(e1, e2);
        return getCoupleFromID(ID);
    }

    public boolean containsCoupleFromID(long ID) {
        return getCoupleFromID(ID) != null;
    }

    public boolean containsCoupleFromEntities(Entity e1, Entity e2) {
        if(e1 == null || e2 == null)
            throw new IllegalArgumentException("Entities cannot be null");

        long ID = PlayerCouple.uniqueIDFromEntityPair(e1, e2);
        return getCoupleFromID(ID) != null;
    }

    public boolean containsCouple(PlayerCouple couple) {
        return _couples.containsKey(couple.uniqueID());
    }

    public void clear() {
        _couples.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return _couples.iterator();
    }

    public int size() {
        return _couples.size();
    }

    public void removeCouple(long id) {
        _couples.remove(id);
    }
}
