package com.shnud.noxray.Structures.ByteWrappers;

import com.shnud.noxray.Structures.ByteArray;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NibbleArray extends ByteArrayWrapper {

    private final NibbleOrder _order;
    public static enum NibbleOrder {
        EVEN_ON_LEFT,
        EVEN_ON_RIGHT;
    }

    public NibbleArray(int length, NibbleOrder order) {
        super(length);
        _order = order;
    }

    public NibbleArray(ByteArray array, NibbleOrder order) {
        super(array);
        _order = order;
    }

    public NibbleArray(ByteArray array) {
        this(array, NibbleOrder.EVEN_ON_LEFT);
    }

    @Override
    public int getValueAtIndex(int index) {
        if(index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        boolean even = (index & 1) == 0;

        if(even && _order == NibbleOrder.EVEN_ON_LEFT || !even && _order == NibbleOrder.EVEN_ON_RIGHT)
            return (getByteArray().getValueAtIndex(index / 2) & 0xF0) >> 4;
        else
            return (getByteArray().getValueAtIndex(index / 2) & 0x0F);
    }

    @Override
    public void setValueAtIndex(int index, int value) {
        if(value > maxValue())
            throw new IllegalArgumentException("Nibble cannot store numbers larger than 15");

        if(index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        boolean even = (index & 1) == 0;
        index /= 2;

        if(even && _order == NibbleOrder.EVEN_ON_LEFT || !even && _order == NibbleOrder.EVEN_ON_RIGHT) {
            value <<= 4;
            // Erase values to the left of original value
            getByteArray().setValueAtIndex(index, (byte) (getByteArray().getValueAtIndex(index) & 15));
            // Replace with new values by ORing
            getByteArray().setValueAtIndex(index, (byte) (getByteArray().getValueAtIndex(index) | value));
        }
        else {
            // Erase values to the right of original value
            getByteArray().setValueAtIndex(index, (byte) (getByteArray().getValueAtIndex(index) & 240));
            // Replace with new values by ORing
            getByteArray().setValueAtIndex(index, (byte) (getByteArray().getValueAtIndex(index) | value));
        }
    }

    @Override
    public int size() {
        return getByteArray().size() * 2;
    }

    @Override
    public int maxValue() {
        return 15;
    }

    @Override
    public int bitsPerValue() {
        return 4;
    }
}
