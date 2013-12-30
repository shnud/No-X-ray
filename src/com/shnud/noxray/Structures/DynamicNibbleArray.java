package com.shnud.noxray.Structures;

import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 30/12/2013.
 */
public class DynamicNibbleArray {

    private DynamicByteArray _byteArray;
    private int _nibbleLength;

    public DynamicNibbleArray(int length) {
        _nibbleLength = length;
        int dividedLength = (length / 2) + (length % 2 == 0 ? 0 : 1);
        _byteArray = DynamicByteArray.constructFromUncompressedByteArray(new byte[dividedLength]);
    }

    private DynamicNibbleArray(byte[] existingArray, boolean compressed, int nibbleLength) throws DataFormatException {
        if(!compressed && nibbleLength > existingArray.length * 2)
            throw new IllegalArgumentException("There can only be twice as many nibbles in a byte array as there are bytes");

        _nibbleLength = nibbleLength;

        if(!compressed)
            _byteArray = DynamicByteArray.constructFromUncompressedByteArray(existingArray);
        else
            _byteArray = DynamicByteArray.constructFromCompressedByteArray(existingArray);
    }

    public static DynamicNibbleArray constructFromExistingCompressedArray(byte[] compressed, int nibbleLength) throws DataFormatException {
        return new DynamicNibbleArray(compressed, true, nibbleLength);
    }

    public static DynamicNibbleArray constructFromExistingArray(byte[] uncompressed, int nibbleLength) {
        try {
            return new DynamicNibbleArray(uncompressed, true, nibbleLength);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getValueAtIndex(int index) {
        if(index >= _nibbleLength)
            throw new ArrayIndexOutOfBoundsException();

        boolean even = (index & 1) == 0;

        if(even)
            return (_byteArray.getValueAtIndex(index / 2) & 0xF0) >> 4;
        else
            return (_byteArray.getValueAtIndex(index / 2) & 0x0F);
    }

    public void setValueAtIndex(int index, byte value) {
        if(value > 15)
            throw new IllegalArgumentException("Nibble cannot store numbers larger than 15");

        if(index >= _nibbleLength)
            throw new ArrayIndexOutOfBoundsException();

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

    public final int getLength() {
        return _nibbleLength;
    }

    public byte[] getCompressedByteArray() {
        return _byteArray.getCompressedByteArray();
    }
}
