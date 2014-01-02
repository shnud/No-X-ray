package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NibbleArray implements ByteArrayWrapper {

    private ByteArray _byteArray;

    public NibbleArray(int length) {
        int dividedLength = (length / 2) + (length % 2 == 0 ? 0 : 1);
        _byteArray = new ByteArray(dividedLength);
    }

    public NibbleArray(ByteArray array) {
        _byteArray = array;
    }

    @Override
    public int getValueAtIndex(int index) {
        if(index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        boolean even = (index & 1) == 0;

        if(even)
            return (_byteArray.getValueAtIndex(index / 2) & 0xF0) >> 4;
        else
            return (_byteArray.getValueAtIndex(index / 2) & 0x0F);
    }

    @Override
    public void setValueAtIndex(int index, int value) {
        if(value > maxValue())
            throw new IllegalArgumentException("Nibble cannot store numbers larger than 15");

        if(index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        boolean even = (index & 1) == 0;
        index /= 2;

        if(even)
        {
            value <<= 4; // Even indexed first, so move them to the left

            // Erase values to the left of original value
            _byteArray.setValueAtIndex(index, (byte) (_byteArray.getValueAtIndex(index) & 15));
            // Replace with new values by ORing
            _byteArray.setValueAtIndex(index, (byte) (_byteArray.getValueAtIndex(index) | value));
        }
        else
        {
            // Odd indexed last, and no values on the left since
            // we checked if it was greater than 15
            _byteArray.setValueAtIndex(index, (byte) (_byteArray.getValueAtIndex(index) & 240));
            // Replace with new values by ORing
            _byteArray.setValueAtIndex(index, (byte) (_byteArray.getValueAtIndex(index) | value));
        }
    }

    @Override
    public int size() {
        return _byteArray.size() * 2;
    }

    @Override
    public void clear() {
        for(int i = 0; i < size(); i++) setValueAtIndex(i, 0);
    }

    @Override
    public ByteArray getByteArray() {
        return _byteArray;
    }

    @Override
    public void setByteArray(ByteArray array) {
        _byteArray = _byteArray;
    }

    @Override
    public int maxValue() {
        return 15;
    }

    @Override
    public byte bitsPerValue() {
        return 4;
    }
}
