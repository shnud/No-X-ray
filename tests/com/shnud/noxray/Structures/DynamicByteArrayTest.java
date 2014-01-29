package com.shnud.noxray.Structures;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DynamicByteArrayTest {

    private DynamicByteArray _array;
    private static final int SIZE = 4096;

    public DynamicByteArrayTest(DynamicByteArray array) {
        _array = array;
    }

    @Parameterized.Parameters
    public static Collection parameters() {
        return Arrays.asList(new Object[][]{
                {DynamicByteArray.newWithUncompressedArray(new byte[SIZE])},
                {new DynamicByteArray(SIZE)},
                {new DynamicByteArray(new byte[SIZE])}
        });
    }

    @Test
    public void setAndGet() {
        _array.setValueAtIndex(15, (byte) 97);
        assertEquals((byte) 97, _array.getValueAtIndex(15));
    }

    @Test
    public void setAndGetWithCompressedInput() {
        _array.setValueAtIndex(200, (byte) 5);

        DynamicByteArray compressedInput = DynamicByteArray.newWithCompressedArray(_array
                .getCompressedPrimitiveByteArray());

        assertEquals((byte) 5, compressedInput.getValueAtIndex(200));
    }

    @Test
    public void sizeWithCompressedInput() {
        DynamicByteArray compressedInput = DynamicByteArray.newWithCompressedArray(_array
                .getCompressedPrimitiveByteArray());

        assertEquals(SIZE, compressedInput.size());
    }

    @Ignore
    public void checkCompressesAfterInterval() {
        int timeToSleep = _array.getIntervalBetweenCompression() * 1000;
        _array.setValueAtIndex(20, (byte) 65);

        try {
            Thread.sleep(timeToSleep + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(_array.isCompressed(), true);
    }

    @Ignore
    public void checkWillNotCompressWhileInUse() {
        int compressionInterval = _array.getIntervalBetweenCompression();

        int i = 0;
        while(i < compressionInterval + 5) {
            try {
                Thread.sleep(1000);

                // Let it know we're still using it
                _array.getValueAtIndex(0);
                assertEquals(_array.isCompressed(), false);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }

        assertEquals(_array.isCompressed(), false);
    }

    @Test
    public void checkCompressionAndUncompression() {
        _array.setValueAtIndex(250, (byte) 61);
        _array.forceCompress();
        assertEquals(_array.isCompressed(), true);
        _array.forceUncompress();
        assertEquals(_array.isCompressed(), false);
        assertEquals(_array.getValueAtIndex(250), (byte) 61);
    }

    @Test
    public void sizeIsFullSize() {
        assertEquals(_array.size(), SIZE);
        assertEquals(_array.getValueAtIndex(SIZE - 1), _array.getValueAtIndex(SIZE - 1));
    }
}
