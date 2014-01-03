package com.shnud.noxray.Structures;

import java.util.*;

/**
 * Created by Andrew on 23/12/2013.
 */
public class HashMapArrayList<K, T> implements Iterable<T> {

    protected ArrayList<T> _list;
    protected HashMap<K, T> _hashMap;

    public HashMapArrayList() {
        _list = new ArrayList<T>();
        _hashMap = new HashMap<K, T>();
    }

    public void put(K key, T object) {
        if(_hashMap.containsKey(object))
            return;

        _list.add(object);
        _hashMap.put(key, object);
    }

    public void remove(K key) {
        if(!_hashMap.containsKey(key))
            return;

        T object = _hashMap.get(key);
        _hashMap.remove(key);
        _list.remove(object);
    }

    public T get(K key) {
        return _hashMap.get(key);
    }

    public int size() {
        return _list.size();
    }

    public boolean containsKey(K key) {
        return _hashMap.containsKey(key);
    }

    @Override
    public Iterator<T> iterator() {
        return new HashMapArrayListIterator();
    }

    public void clear() {
        _hashMap.clear();
        _list.clear();
    }

    public boolean isEmpty() {
        return _list.isEmpty();
    }

    private Set<K> getSet() {
        return _hashMap.keySet();
    }

    private class HashMapArrayListIterator implements Iterator<T> {

        private int index = -1;

        @Override
        public boolean hasNext() {
            if(index + 1 < _list.size())
                return true;

            return false;
        }

        @Override
        public T next() {
            return _list.get(++index);
        }

        @Override
        public void remove() {
            _hashMap.remove(_list.get(index));
            _list.remove(index);
            index--;
        }
    }
}
