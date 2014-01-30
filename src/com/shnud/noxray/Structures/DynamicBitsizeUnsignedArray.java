package com.shnud.noxray.Structures;

import com.shnud.noxray.Structures.ByteWrappers.VariableBitsizeUnsignedArray;

/**
 * An array which dynamically shrinks/grows the amount of bits used to store numbers
 * in order to save space.
 *
 * By default, the array will start storing numbers in only 2 bits, with a maximum value
 * of 3. If the caller attempts to set a value higher than 3, the underlying bit array
 * will expand to use 3 bits to store a maximum value of 7.
 *
 * If shrinking is enabled, the array will keep track of how many values have actually gone
 * past the previous size, and if it returns back to 0, the array will be shrunk back down
 * again.
 */
public class DynamicBitsizeUnsignedArray {

    private static final int DEFAULT_INITIAL_BITS_PER_VALUE = 2;
    // Max bit size is same size as integer, which we're using to
    // get and set, so there's no point going higher than this
    private static final int DEFAULT_MAX_BITS_PER_VALUE = 32;
    private final int _bitPerValueLimit;
    private final boolean _autoShrink;
    private VariableBitsizeUnsignedArray _bitArray;
    // For autoshrinking we ignore that value at index 0 as it
    // only indicates how many values are empty which isn't really
    // useful to us (yet?)
    private int[] valuesPerBitSize;

    /**
     * Creates a new dynamic number array
     * @param size the size of the array
     * @param initialBitsPerValue the initial amount of bits used to store values
     * @param bitsPerValueLimit the maximum amount of bits the array will grow to to store values
     * @param autoShrink whether to shrink again once the array no longer holds values requring the current bit size
     */
    public DynamicBitsizeUnsignedArray(int size, int initialBitsPerValue, int bitsPerValueLimit, boolean autoShrink) {
        _bitArray = new VariableBitsizeUnsignedArray(initialBitsPerValue, size);
        _bitPerValueLimit = bitsPerValueLimit;
        _autoShrink = autoShrink;

        if(_autoShrink) {
            valuesPerBitSize = new int[initialBitsPerValue + 1];
        }
    }

    /**
     * Creates a new dynamic number array with default settings
     * @param size the size of the array
     */
    public DynamicBitsizeUnsignedArray(int size) {
        this(size, DEFAULT_INITIAL_BITS_PER_VALUE, DEFAULT_MAX_BITS_PER_VALUE, true);
    }

    /**
     * Create a new dynamic number array from a preexisting VariableBitsizeUnsignedArray
     * @param existingArray the preexisting array
     * @param bitsPerValueLimit the maximum amount of bits the array will grow to to store values
     * @param autoShrink whether to shrink again once the array no longer holds values requring the current bit size
     */
    public DynamicBitsizeUnsignedArray(VariableBitsizeUnsignedArray existingArray, int bitsPerValueLimit, boolean autoShrink) {
        _bitArray = existingArray;
        _bitPerValueLimit = bitsPerValueLimit;
        _autoShrink = autoShrink;

        populateValuesStoredPerBitSize();
    }

    /**
     * Creates a new dynamic number array from a preexisting VariableBitsizeUnsignedArray with default settings
     * @param existingArray the preexisting array
     */
    public DynamicBitsizeUnsignedArray(VariableBitsizeUnsignedArray existingArray) {
        this(existingArray, DEFAULT_MAX_BITS_PER_VALUE, true);
    }

    public int size() {
        return _bitArray.size();
    }

    public int getCurrentBitsPerValue() {
        return _bitArray.bitsPerValue();
    }

    public void setValueAtIndex(int index, int value) {
        if(value < 0)
            throw new IllegalArgumentException("Value must be unsigned");

        int oldBitSize = minimumBitsRequiredToStoreValue(_bitArray.getValueAtIndex(index));
        int newBitSize = minimumBitsRequiredToStoreValue(value);

        if(value > _bitArray.maxValue())
            shrinkOrGrowTo(newBitSize);
        _bitArray.setValueAtIndex(index, value);

        if(_autoShrink) {
            valuesPerBitSize[oldBitSize] -= 1;
            valuesPerBitSize[newBitSize] += 1;

            if(valuesPerBitSize[oldBitSize] == 0 && oldBitSize == valuesPerBitSize.length - 1) {
                shrinkAccordingToLargestUsedBitSize();
            }
        }
    }

    public int getValueAtIndex(int index) {
        return _bitArray.getValueAtIndex(index);
    }

    public void forceShrinkAttempt() {
        populateValuesStoredPerBitSize();
        shrinkAccordingToLargestUsedBitSize();
    }

    public int getCurrentMaxPossibleValue() {
        return _bitArray.maxValue();
    }

    private void shrinkOrGrowTo(int bits) {
        if(bits != _bitArray.bitsPerValue()) {
            _bitArray = _bitArray.convertTo(bits);

            if(_autoShrink)
                shrinkOrGrowValuesPerBitSizeArray(bits);
        }
    }

    private void shrinkOrGrowValuesPerBitSizeArray(int bits) {
        int[] newArray = new int[bits + 1];

        for(int i = 0; i < newArray.length; i++) {
            if(i < valuesPerBitSize.length)
                newArray[i] = valuesPerBitSize[i];
            else
                newArray[i] = 0;
        }

        valuesPerBitSize = newArray;
    }

    private int minimumBitsRequiredToStoreValue(int value) {
        for (int i = 31; i >= 0; i--) {
            if((value & (1 << i)) != 0) return i + 1;
        }
        return 0;
    }

    private void populateValuesStoredPerBitSize() {
        valuesPerBitSize = new int[_bitArray.bitsPerValue() + 1];
        for(int i = 0; i < _bitArray.size(); i++) {
            valuesPerBitSize[minimumBitsRequiredToStoreValue(_bitArray.getValueAtIndex(i))] += 1;
        }
    }

    private void shrinkAccordingToLargestUsedBitSize() {
        int largestBitSize = getLargestUtilisedBitSize();

        // We can't shrink to 0 bit size because we wouldn't have an array at all
        // so even if the array is empty of anything above 0 we must still use 1
        if (largestBitSize == 0)
            shrinkOrGrowTo(1);
        else
            shrinkOrGrowTo(largestBitSize);
    }

    private int getLargestUtilisedBitSize() {
        for(int i = valuesPerBitSize.length - 1; i >= 0; i--) {
            if(valuesPerBitSize[i] > 0) return i;
        }

        return 0;
    }
}
