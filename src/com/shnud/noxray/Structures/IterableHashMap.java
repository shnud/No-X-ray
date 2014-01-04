package com.shnud.noxray.Structures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Andrew on 04/01/2014.
 */
public class IterableHashMap<K, V> implements Iterable<V> {

    private HashMap<K, V> _map = new HashMap<K, V>();

    public void put(K key, V value) {
        _map.put(key, value);
    }

    public V get(K key) {
        return _map.get(key);
    }

    public int size() {
        return _map.size();
    }

    public boolean containsKey(K key) {
        return _map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return _map.containsValue(value);
    }

    public void remove(K key) {
        _map.remove(key);
    }

    public void clear() {
        _map.clear();
    }

    @Override
    public Iterator<V> iterator() {
        return new HashMapIterator();
    }

    public class HashMapIterator implements Iterator<V> {

        private Iterator<K> _keyIt;
        private K _currentKey;

        public HashMapIterator() {
            Set<K> keySet = _map.keySet();
            _keyIt = keySet.iterator();
        }

        @Override
        public boolean hasNext() {
            return _keyIt.hasNext();
        }

        @Override
        public V next() {
            _currentKey = _keyIt.next();
            return _map.get(_currentKey);
        }

        @Override
        public void remove() {
            _keyIt.remove();
            _map.remove(_currentKey);
        }
    }
}
