package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 22/12/2013.
 */
public class BooleanArray {

    private byte[] _byteArray;
    private int _amountOfBools;

    public BooleanArray(int length) {
        _amountOfBools = length;
        int dividedLength = length / 8 + (length % 8 == 0 ? 0 : 1);
        _byteArray = new byte[dividedLength];
    }

    public BooleanArray(byte[] existingArray) {
        _amountOfBools = existingArray.length * 8;
        _byteArray = existingArray;
    }

    public boolean getValueAtIndex(int index) {
        if(index >= _amountOfBools)
            throw new ArrayIndexOutOfBoundsException();

        return (_byteArray[index / 8] & (0x80 >> (index % 8))) != 0;
    }

    public void setValueAtIndex(int index, boolean value) {
        if(index >= _amountOfBools)
            throw new ArrayIndexOutOfBoundsException();

        _byteArray[index / 8] &= ~(0x80 >> (index % 8));

        if(value)
            _byteArray[index / 8] |= (0x80 >> (index % 8));
    }

    public int getLength() {
        return _amountOfBools;
    }

    public byte[] getByteArray() {
        return _byteArray;
    }
}
