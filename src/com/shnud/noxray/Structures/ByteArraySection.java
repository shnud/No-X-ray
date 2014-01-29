package com.shnud.noxray.Structures;

/**
 * Wraps around a byte array and allows access to only a certain portion of it,
 * as if the portion specified was a standalone byte array.
 */
public class ByteArraySection extends ByteArray {

    private final int _offset;
    private final int _length;

    /**
     * Construct a byte array section
     * @param array the byte array to wrap around
     * @param offset the offset at which this section of the underlying array should start
     * @param length the length of this section of the underlying byte array
     */
    public ByteArraySection(byte[] array, int offset, int length) {
        super(array);

        if(offset < 0 || length < 1 || offset > array.length - 1 || offset + length > array.length)
            throw new IllegalArgumentException("Offset and/or length cannot be beyond original array dimensions");

        _offset = offset;
        _length = length;
    }

    /**
     * Gets the value at the index relative to this section of the underlying byte array
     * @param index the relative index of this section to get the value at
     * @return the value at the given index
     */
    @Override
    public byte getValueAtIndex(int index) {
        int wrappedIndex = index + _offset;

        if(wrappedIndex > _offset + _length || wrappedIndex < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        return super.getValueAtIndex(wrappedIndex);
    }

    /**
     * Sets the value at the index relative to this section of the underlying byte array
     * @param index the relative index of this section to set the value at
     * @param value the value to set at the index
     */
    @Override
    public void setValueAtIndex(int index, byte value) {
        int wrappedIndex = index + _offset;

        if(wrappedIndex > _offset + _length || wrappedIndex < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        super.setValueAtIndex(wrappedIndex, value);
    }

    /**
     * Get the size of this section (not the underlying array)
     * @return the amount of bytes in this section of the underlying byte array
     */
    @Override
    public int size() {
        return _length;
    }

    /**
     * Set all of the values in this section of the underlying byte array to 0
     */
    @Override
    public void clear() {
        for (int i = _offset; i < _offset + _length; i++) {
            getPrimitiveByteArray()[i] = 0;
        }
    }
}
