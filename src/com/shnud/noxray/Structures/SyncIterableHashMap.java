package com.shnud.noxray.Structures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Andrew on 04/01/2014.
 */
public class SyncIterableHashMap<K, V> extends IterableHashMap<K, V> implements Iterable<V> {

    private HashMap<K, V> _map = new HashMap<K, V>();

    synchronized public void put(K key, V value) {
        _map.put(key, value);
    }

    synchronized public V get(K key) {
        return _map.get(key);
    }

    synchronized public int size() {
        return _map.size();
    }

    synchronized public boolean containsKey(K key) {
        return _map.containsKey(key);
    }

    synchronized public boolean containsValue(V value) {
        return _map.containsValue(value);
    }

    synchronized public void remove(K key) {
        _map.remove(key);
    }

    synchronized public void clear() {
        _map.clear();
    }

    @Override
    public Iterator<V> iterator() {
        return new HashMapIterator();
    }

    private class HashMapIterator implements Iterator<V> {

        private Iterator<K> _keyIt;
        private K _currentKey;

        public HashMapIterator() {
            synchronized (SyncIterableHashMap.this) {
                Set<K> keySet = _map.keySet();
                _keyIt = keySet.iterator();
            }
        }

        @Override
        public boolean hasNext() {
            synchronized (SyncIterableHashMap.this) {
                return _keyIt.hasNext();
            }
        }

        @Override
        public V next() {
            synchronized (SyncIterableHashMap.this) {
                _currentKey = _keyIt.next();
                return _map.get(_currentKey);
            }
        }

        @Override
        public void remove() {
            synchronized (SyncIterableHashMap.this) {
                _keyIt.remove();
                _map.remove(_currentKey);
            }
        }
    }
}
