package com.shnud.noxray.Structures;

import com.shnud.noxray.Utilities.MathHelper;

/**
 * Created by Andrew on 01/01/2014.
 */
public class ByteBitWrapper {

    private ByteArray _byteArray;
    private int _bitsPerVal;
    private int _maxVal;

    protected void setBitsPerVal(int bits) {
        if(bits < 1)
            throw new IllegalArgumentException("Cannot have a zero bit value");

        _bitsPerVal = bits;
        _maxVal = (int) MathHelper.pow(2, _bitsPerVal) - 1;
    }

    protected void setByteArray(ByteArray array) {
        _byteArray = array;
    }

    public int getBitsPerVal() {
        return _bitsPerVal;
    }

    ByteBitWrapper(int bitsPerVal, int size) {
        this(bitsPerVal, new ByteArray(sizeRequiredFor(size, bitsPerVal)));
    }

    ByteBitWrapper(int bitsPerVal, ByteArray array) {
        if(array == null)
            throw new IllegalArgumentException("Array cannot be null");

        setBitsPerVal(bitsPerVal);
        _byteArray = array;
    }

    /**
     * Returns the amount of values at the currently set bit-size can be held by
     * this array
     */
    public int size() {
        return _byteArray.size() * 8 / _bitsPerVal;
    }

    /**
     * Returns the actual size of the byte array which is being wrapped by this object
     */
    public int realSize() {
        return _byteArray.size();
    }

    public int maxValue() {
        return _maxVal;
    }

    public ByteArray getByteArray() {
        return _byteArray;
    }

    public void setValueAtIndex(int index, int value) {
        if(value > maxValue())
            throw new IllegalArgumentException("Value too large to store in " + _bitsPerVal + " bits");

        int bitIndex = index * _bitsPerVal;
        int byteIndex = bitIndex / 8;

        for(int i = 0; i < _bitsPerVal; i++) {
            int offset = bitIndex % 8;

            boolean bitIsSet = value >> (_bitsPerVal - 1 - i) == 1;

            byte get = _byteArray.getValueAtIndex(byteIndex);
            if(bitIsSet)
                _byteArray.setValueAtIndex(byteIndex, (byte) (get | (1 << 7 - offset)));
            else
                _byteArray.setValueAtIndex(byteIndex, (byte) (get & (0 << 7 - offset)));

            if(offset == 7)
                byteIndex++;
        }
    }

    public int getValueAtIndex(int index) {
        int bitIndex = index * _bitsPerVal;
        int byteIndex = bitIndex / 8;
        int result = 0;

        for(int i = 0; i < _bitsPerVal; i++) {
            int offset = bitIndex % 8;

            result |= (_byteArray.getValueAtIndex(index) >> (7 - offset) & 1) << _bitsPerVal - 1 - i;

            if(offset == 7)
                byteIndex++;
        }

        return result;
    }

    public void convertTo(int bitsVerVal) {
        byte[] newArray = convertWrappedByteArray(_byteArray.getPrimitiveByteArray(), _bitsPerVal, bitsVerVal);
        _byteArray = new ByteArray(newArray);
        setBitsPerVal(bitsVerVal);
    }

    protected static byte[] convertWrappedByteArray(byte[] input, int inputBitsPerVal, int outputBitsPerVal) {
        if(inputBitsPerVal == outputBitsPerVal)
            return input;

        int valuesInInput = (input.length * 8 / inputBitsPerVal);
        byte[] output = new byte[sizeRequiredFor(valuesInInput, outputBitsPerVal)];

        for(int i = 0; i < valuesInInput; i++) {
            int inputBitIndex = (i * inputBitsPerVal) + inputBitsPerVal - 1;
            int outputBitIndex = (i * outputBitsPerVal) + outputBitsPerVal - 1;
            int inputByte = inputBitIndex / 8;
            int outputByte = outputBitIndex / 8;

            for(int bit = 0; i < inputBitsPerVal && i < outputBitsPerVal; i++) {
                int inputOffset = inputBitIndex % 8;
                int outputOffset = outputBitIndex % 8;

                boolean one = (input[inputBitIndex] & (1 << 7 - inputOffset)) == 1;
                if(one)
                    output[outputBitIndex] |= (1 << 7 - outputOffset);

                inputBitIndex--;
                outputBitIndex--;

                if(inputOffset == 0) inputByte--;
                if(outputOffset == 0) outputByte--;
            }
        }

        return output;
    }

    protected static int sizeRequiredFor(int size, int bitsPerVal) {
        return ((size * bitsPerVal) / 8) + (((size * bitsPerVal) % 8) == 0 ? 0 : 1);
    }

    public static int getMaxValueOfBitLengthValue(int bits) {
        return (int) MathHelper.pow(2, bits);
    }
}
