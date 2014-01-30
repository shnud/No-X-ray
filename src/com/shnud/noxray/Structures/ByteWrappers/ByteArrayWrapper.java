package com.shnud.noxray.Structures.ByteWrappers;

import com.shnud.noxray.Structures.ByteArray;
import com.shnud.noxray.Utilities.MathHelper;

/**
 * Created by Andrew on 01/01/2014.
 */
public abstract class ByteArrayWrapper {
    private final ByteArray _array;
    public ByteArrayWrapper(ByteArray array) {
        if(array == null)
            throw new IllegalArgumentException("Array cannot be null");

        _array = array;
    }
    public ByteArrayWrapper(int length) {
        _array = new ByteArray(MathHelper.divisionCeiling(length * bitsPerValue(), 8));
    }
    protected final ByteArray getByteArray() {
        return _array;
    }
    public abstract int getValueAtIndex(int index);
    public abstract void setValueAtIndex(int index, int value);
    public abstract int size();
    public void clear() {
        _array.clear();
    }
    public abstract int maxValue();
    public abstract int bitsPerValue();
}
