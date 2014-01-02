package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 22/12/2013.
 */
public class BooleanArray {

    private byte[] _byteArray;
    private int _sizeInBools;

    public BooleanArray(int length) {
        _sizeInBools = length;
        int dividedLength = length / 8 + (length % 8 == 0 ? 0 : 1);
        _byteArray = new byte[dividedLength];
    }

    public BooleanArray(byte[] existingArray) {
        _sizeInBools = existingArray.length * 8;
        _byteArray = existingArray;
    }

    public boolean getValueAtIndex(int index) {
        if(index >= _sizeInBools)
            throw new ArrayIndexOutOfBoundsException();

        return (_byteArray[index / 8] & (0x80 >> (index % 8))) != 0;
    }

    public void setValueAtIndex(Boolean value, int index) {
        if(index >= _sizeInBools)
            throw new ArrayIndexOutOfBoundsException();

        _byteArray[index / 8] &= ~(0x80 >> (index % 8));

        if(value)
            _byteArray[index / 8] |= (0x80 >> (index % 8));
    }

    public int size() {
        return _sizeInBools;
    }

    public void clear() {

    }

    public byte[] getByteArray() {
        return _byteArray;
    }
}
