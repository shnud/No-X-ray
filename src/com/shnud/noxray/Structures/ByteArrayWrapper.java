package com.shnud.noxray.Structures;

/**
 * Created by Andrew on 01/01/2014.
 */
public interface ByteArrayWrapper {
    public int getValueAtIndex(int index);
    public void setValueAtIndex(int index, int value);
    public int size();
    public void clear();
    public ByteArray getByteArray();
    public void setByteArray(ByteArray array);
    public int maxValue();
    public byte bitsPerValue();
}
