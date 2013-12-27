package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 22/12/2013.
 */
public class NibbleArray {

    private byte[] _byteArray;
    private int _nibbleLength;

    public NibbleArray(int length) {
        _nibbleLength = length;
        int dividedLength = (length / 2) + (length % 2 == 0 ? 0 : 1);
        _byteArray = new byte[dividedLength];
    }

    public NibbleArray(byte[] existingArray) {
        _nibbleLength = existingArray.length * 2;
        _byteArray = existingArray;
    }

    public int getValueAtIndex(int index) {
        if(index >= _nibbleLength)
            throw new ArrayIndexOutOfBoundsException();

        boolean even = (index & 1) == 1;

        if(even)
            return (_byteArray[index / 2] & 0xF0) >> 4;
        else
            return (_byteArray[index / 2] & 0x0F);
    }

    public void setValueAtIndex(int index, byte value) {
        if(value > 15)
            throw new IllegalArgumentException("Nibble cannot store numbers larger than 15");

        if(index >= _nibbleLength)
            throw new ArrayIndexOutOfBoundsException();

        boolean even = (index & 1) == 1;

        if(even)
        {
            value <<= 4; // Even indexed first, so move them to the left

            // Erase values to the left of original value
            _byteArray[index / 2] &= 15;
            // Replace with new values by ORing
            _byteArray[index / 2] |= value;
        }
        else
        {
            // Odd indexed last, and no values on the left since
            // we checked if it was greater than 15
            _byteArray[index / 2] &= 240; // Erase values to the left of original value
            _byteArray[index / 2] |= value; // Replace with new values by ORing
        }
    }

    public int getLength() {
        return _nibbleLength;
    }

    public byte[] getByteArray() {
        return _byteArray;
    }
}
