package com.shnud.noxray.Structures.ByteWrappers;

import com.shnud.noxray.Structures.ByteArray;
import com.shnud.noxray.Utilities.MathHelper;

/**
 * Created by Andrew on 01/01/2014.
 */
public class VariableBitsizeUnsignedArray extends ByteArrayWrapper {

    private final int _bitsPerVal;
    private final int _maxVal;
    private final int _size;

    public VariableBitsizeUnsignedArray(int bitsPerVal, int size) {
        this(bitsPerVal, new ByteArray(bytesRequiredFor(size, bitsPerVal)), size);
    }

    public VariableBitsizeUnsignedArray(int bitsPerVal, ByteArray array, int size) {
        super(array);
        if(bitsPerVal < 1)
            throw new IllegalArgumentException("Cannot have a zero bit value");

        _bitsPerVal = bitsPerVal;
        _maxVal = getMaxValueOfBitLengthValue(_bitsPerVal);
        _size = size;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public int maxValue() {
        return _maxVal;
    }

    @Override
    public int bitsPerValue() {
        return _bitsPerVal;
    }

    @Override
    public void setValueAtIndex(int index, int value) {
        if(value > maxValue())
            throw new IllegalArgumentException("Value too large to store in " + _bitsPerVal + " bits");
        if(index >= _size)
            throw new ArrayIndexOutOfBoundsException(index);

        int bitIndex = index * _bitsPerVal;
        int byteIndex = bitIndex / 8;

        for(int i = 0; i < _bitsPerVal; i++) {
            int offset = bitIndex % 8;

            boolean bitIsSet = ((value >> (_bitsPerVal - 1 - i)) & 1) == 1;

            byte get = getByteArray().getValueAtIndex(byteIndex);
            if(bitIsSet)
                getByteArray().setValueAtIndex(byteIndex, (byte) (get | (1 << 7 - offset)));
            else
                getByteArray().setValueAtIndex(byteIndex, (byte) (get & ~(1 << 7 - offset)));

            bitIndex++;

            if(offset == 7)
                byteIndex++;
        }
    }

    @Override
    public int getValueAtIndex(int index) {
        if(index >= _size)
            throw new ArrayIndexOutOfBoundsException(index);

        int bitIndex = index * _bitsPerVal;
        int byteIndex = bitIndex / 8;
        int result = 0;

        for(int i = 0; i < _bitsPerVal; i++) {
            int offset = bitIndex % 8;

            int bit = ((getByteArray().getValueAtIndex(byteIndex) >> (7 - offset)) & 1);
            result |= bit << (_bitsPerVal - 1 - i);

            bitIndex++;

            if(offset == 7)
                byteIndex++;
        }

        return result;
    }

    public VariableBitsizeUnsignedArray convertTo(int bitsVerVal) {
        byte[] newArray = convertWrappedByteArray(

                getByteArray().getPrimitiveByteArray(),
                _size,
                _bitsPerVal,
                bitsVerVal
        );

        return new VariableBitsizeUnsignedArray(bitsVerVal, new ByteArray(newArray), _size);
    }

    /**
     * Converts a variable bit array to another sized bit array non destuctively (unless down scaling, which is not
     * possible)
     *
     * @param input the input array
     * @param inputBitsPerVal the bits per value of the input array
     * @param outputBitsPerVal the desired bits per value of the output array
     * @return the converted byte array with the specified bits ver value amount
     */
    protected static byte[] convertWrappedByteArray(byte[] input, int size, int inputBitsPerVal, int outputBitsPerVal) {
        if(inputBitsPerVal == outputBitsPerVal)
            return input;

        byte[] output = new byte[bytesRequiredFor(size, outputBitsPerVal)];

        for(int i = 0; i < size; i++) {
            int inputBitIndex = (i * inputBitsPerVal) + inputBitsPerVal - 1;
            int outputBitIndex = (i * outputBitsPerVal) + outputBitsPerVal - 1;
            int inputByte = inputBitIndex / 8;
            int outputByte = outputBitIndex / 8;

            for(int bit = 0; bit < inputBitsPerVal && bit < outputBitsPerVal; bit++) {
                int inputOffset = inputBitIndex % 8;
                int outputOffset = outputBitIndex % 8;

                boolean one = (input[inputByte] & (1 << (7 - inputOffset))) != 0;
                if(one)
                    output[outputByte] |= (1 << (7 - outputOffset));

                inputBitIndex--;
                outputBitIndex--;

                if(inputOffset == 0) inputByte--;
                if(outputOffset == 0) outputByte--;
            }
        }

        return output;
    }

    protected static int bytesRequiredFor(int size, int bitsPerVal) {
        return MathHelper.divisionCeiling(size * bitsPerVal, 8);
    }

    protected static int getMaxValueOfBitLengthValue(int bits) {
        return (int) MathHelper.pow(2, bits - 1) - 1;
    }
}
