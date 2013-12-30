package com.shnud.noxray.Utilities;

import java.util.Iterator;

/**
 * Created by Andrew on 29/12/2013.
 */
public class ReadOnlyArray<T> implements Iterable<T> {

    private T[] _array;

    public ReadOnlyArray(T[] array) {
        _array = array;
    }

    public T getValueAtIndex(int index) {
        return _array[index];
    }

    public int getLength() {
        return _array.length;
    }

    @Override
    public Iterator<T> iterator() {
        return new ReadOnlyArrayIterator();
    }

    private class ReadOnlyArrayIterator implements Iterator<T> {

        int index = - 1;

        @Override
        public boolean hasNext() {
            return index + 1 < _array.length;
        }

        @Override
        public T next() {
            return getValueAtIndex(++index);
        }

        @Override
        public void remove() {
            throw new IllegalAccessError("Can't remove from read only array");
        }
    }
}
