package com.shnud.noxray.Structures;

import java.util.zip.DataFormatException;

/**
 * Created by Andrew on 31/12/2013.
 */
public class DynamicSixBitArray {

    private DynamicByteArray _backendArray;
    private int _amountOfSixBits;

    public DynamicSixBitArray(int length) {
        _amountOfSixBits = length;
        int byteArrayLength = (length * 3 / 4) + ((length * 3) % 4 == 0 ? 0 : 1);
        System.out.println(byteArrayLength);
        _backendArray = DynamicByteArray.constructFromUncompressedByteArray(new byte[byteArrayLength]);
    }

    public void setValueAtIndexTo(final int index, final byte value) {
        if(index < 0 || index > _amountOfSixBits - 1)
            throw new ArrayIndexOutOfBoundsException();

        if(value > 63)
            throw new IllegalArgumentException("Six bits cannot store values greater than 63");

        int byteArrayIndex = index * 3 / 4;
        int remainder = (index * 3) % 4;

        switch(remainder) {
            case 0: {
                byte b = _backendArray.getValueAtIndex(byteArrayIndex);
                b &= 3;
                b |= value << 2;
                _backendArray.setValueAtIndex(byteArrayIndex, b);
                break;
            }
            case 1: {

                byte b = _backendArray.getValueAtIndex(byteArrayIndex);
                b &= 192;
                b |= value;
                _backendArray.setValueAtIndex(byteArrayIndex, b);

                break;
            }
            case 2: {

                byte a = _backendArray.getValueAtIndex(byteArrayIndex);
                a &= 240;
                a |= value >> 2;
                _backendArray.setValueAtIndex(byteArrayIndex, a);

                byte b = _backendArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 63;
                b |= value << 6;
                _backendArray.setValueAtIndex(byteArrayIndex + 1, b);

                break;
            }
            case 3: {
                byte a = _backendArray.getValueAtIndex(byteArrayIndex);
                a &= 252;
                a |= value >> 4;
                _backendArray.setValueAtIndex(byteArrayIndex, a);

                byte b = _backendArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 15;
                b |= value << 4;
                _backendArray.setValueAtIndex(byteArrayIndex + 1, b);

                break;

            }
        }
    }

    public int getValueAtIndex(int index) {
        if(index < 0 || index > _amountOfSixBits - 1)
            throw new ArrayIndexOutOfBoundsException();

        int byteArrayIndex = index * 3 / 4;
        int remainder = (index * 3) % 4;

        switch(remainder) {
            case 0: {
                byte b = _backendArray.getValueAtIndex(byteArrayIndex);
                return (b >> 2) & (0xFF >>> 2);
            }
            case 1: {
                byte b = _backendArray.getValueAtIndex(byteArrayIndex);
                return b &= (0xFF >>> 2);
            }
            case 2: {

                byte a = _backendArray.getValueAtIndex(byteArrayIndex);
                a &= 0x0F;
                a <<= 2;

                byte b = _backendArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 0xFF >> 2;
                b >>>= 6;

                return (byte) (a + b);
            }
            case 3: {
                byte a = _backendArray.getValueAtIndex(byteArrayIndex);
                a &= (0x0F >> 2);
                a <<= 4;

                byte b = _backendArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 0xF0;
                b >>>= 4;

                return (a | b) & (0xFF >>> 2);
            }
        }

        // Will never get this far, just keeping IDE happy
        return 0;
    }

    public int size() {
        return _amountOfSixBits;
    }

    public byte[] getBackingArray() throws DataFormatException {
        return _backendArray.getUncompressedByteArray();
    }
}
