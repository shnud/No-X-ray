package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 08/01/2014.
 */
public class ByteArraySection extends ByteArray {

    private final int _offset;
    private final int _length;

    public ByteArraySection(byte[] array, int offset, int length) {
        super(array);

        if(offset < 0 || length < 1 || offset > array.length - 1 || offset + length > array.length)
            throw new IllegalArgumentException();

        _offset = offset;
        _length = length;
    }

    @Override
    public byte getValueAtIndex(int index) {
        int wrappedIndex = index + _offset;

        if(wrappedIndex > _offset + _length || wrappedIndex < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        return super.getValueAtIndex(wrappedIndex);
    }

    @Override
    public void setValueAtIndex(int index, byte value) {
        int wrappedIndex = index + _offset;

        if(wrappedIndex > _offset + _length || wrappedIndex < 0)
            throw new ArrayIndexOutOfBoundsException(index);

        super.setValueAtIndex(wrappedIndex, value);
    }

    @Override
    public int size() {
        return _length;
    }

    @Override
    public void clear() {
        for (int i = _offset; i < _offset + _length; i++) {
            getPrimitiveByteArray()[i] = 0;
        }
    }
}
