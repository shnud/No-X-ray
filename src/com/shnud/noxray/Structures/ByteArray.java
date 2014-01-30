package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 01/01/2014.
 */
public class ByteArray {

    protected byte[] _byteArray;

    public ByteArray(byte[] array) {
        _byteArray = array;
    }

    public ByteArray(int size) {
        _byteArray = new byte[size];
    }

    public byte[] getPrimitiveByteArray() {
        return _byteArray;
    }

    public byte getValueAtIndex(int index) {
        return _byteArray[index];
    }

    public void setValueAtIndex(int index, byte value) {
        _byteArray[index] = value;
    }

    public int size() {
        return _byteArray.length;
    }

    public void clear() {
        for(byte b : _byteArray) b = 0;
    }

    public int maxValue() {
        return 255;
    }

    public ByteArray clone() {
        return new ByteArray(_byteArray.clone());
    }
}
