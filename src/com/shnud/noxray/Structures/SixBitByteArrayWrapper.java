package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 31/12/2013.
 */
public class SixBitByteArrayWrapper implements ByteArrayWrapper {

    private ByteArray _byteArray;

    public SixBitByteArrayWrapper(int length) {
        int byteArrayLength = (length * 3 / 4) + ((length * 3) % 4 == 0 ? 0 : 1);
        _byteArray = new ByteArray(byteArrayLength);
    }

    public SixBitByteArrayWrapper(ByteArray array) {
        _byteArray = array;
    }

    @Override
    public void setValueAtIndex(int index, int value) {
        if(index < 0 || index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        if(value > 63)
            throw new IllegalArgumentException("Six bits cannot store values greater than 63");

        int byteArrayIndex = index * 3 / 4;
        int remainder = (index * 3) % 4;

        switch(remainder) {
            case 0: {
                byte b = _byteArray.getValueAtIndex(byteArrayIndex);
                b &= 3;
                b |= value << 2;
                _byteArray.setValueAtIndex(byteArrayIndex, b);
                break;
            }
            case 1: {

                byte b = _byteArray.getValueAtIndex(byteArrayIndex);
                b &= 192;
                b |= value;
                _byteArray.setValueAtIndex(byteArrayIndex, b);

                break;
            }
            case 2: {

                byte a = _byteArray.getValueAtIndex(byteArrayIndex);
                a &= 240;
                a |= value >> 2;
                _byteArray.setValueAtIndex(byteArrayIndex, a);

                byte b = _byteArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 63;
                b |= value << 6;
                _byteArray.setValueAtIndex(byteArrayIndex + 1, b);

                break;
            }
            case 3: {
                byte a = _byteArray.getValueAtIndex(byteArrayIndex);
                a &= 252;
                a |= value >> 4;
                _byteArray.setValueAtIndex(byteArrayIndex, a);

                byte b = _byteArray.getValueAtIndex(byteArrayIndex + 1);
                b &= 15;
                b |= value << 4;
                _byteArray.setValueAtIndex(byteArrayIndex + 1, b);

                break;

            }
        }
    }

    @Override
    public int getValueAtIndex(int index) {
        if(index < 0 || index >= size())
            throw new ArrayIndexOutOfBoundsException(index);

        int byteArrayIndex = index * 3 / 4;
        int remainder = (index * 3) % 4;

        switch(remainder) {
            case 0: {
                byte b = _byteArray.getValueAtIndex(byteArrayIndex);
                return (b >> 2) & 0x3F;
            }
            case 1: {
                byte b = _byteArray.getValueAtIndex(byteArrayIndex);
                return b &= 0x3F;
            }
            case 2: {
                byte a = _byteArray.getValueAtIndex(byteArrayIndex);
                a <<= 2;
                a &= 0x3C;

                byte b = _byteArray.getValueAtIndex(byteArrayIndex + 1);
                b >>= 6;
                b &= 3;

                return (a | b) & (0xFF);
            }
            case 3: {
                byte a = _byteArray.getValueAtIndex(byteArrayIndex);
                a <<= 4;
                a &= 0x30;

                byte b = _byteArray.getValueAtIndex(byteArrayIndex + 1);
                b >>= 4;
                b &= 0x0F;

                return (a | b) & (0xFF);
            }
        }

        // Will never get this far, just keeping IDE happy
        return 0;
    }

    @Override
    public int size() {
        return _byteArray.size() * 4 / 3;
    }

    @Override
    public void clear() {

    }

    @Override
    public ByteArray getByteArray() {
        return _byteArray;
    }

    @Override
    public void setByteArray(ByteArray array) {
        _byteArray = array;
    }

    @Override
    public int maxValue() {
        return 63;
    }

    @Override
    public byte bitsPerValue() {
        return 6;
    }
}
