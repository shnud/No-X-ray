package com.shnud.noxray.Structures;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class DynamicBitsizeUnsignedArrayTest {
    private DynamicBitsizeUnsignedArray array;

    @Before
    public void setUp() throws Exception {
        array = new DynamicBitsizeUnsignedArray(250);
    }

    @Test
    public void testSetValueAtIndex() throws Exception {
        for (int i = 0; i < 250; i++) {
            array.setValueAtIndex(100, i);
            assertEquals(array.getValueAtIndex(100), i);
            array.setValueAtIndex(i, 100);
            assertEquals(array.getValueAtIndex(i), 100);
        }
    }

    @Test
    public void testSetValueAtIndexAutoShrink() throws Exception {
        array = new DynamicBitsizeUnsignedArray(250, 2, 32, true);
        for (int i = 0; i < 250; i++) {
            array.setValueAtIndex(i, 100);
            assertEquals(array.getValueAtIndex(i), 100);
            array.setValueAtIndex(100, i);
            assertEquals(array.getValueAtIndex(100), i);
        }

        for (int i = 249; i >= 0; i--) {
            array.setValueAtIndex(i, 0);
            assertEquals(array.getValueAtIndex(i), 0);
        }
    }

    @Test
    public void testRandomSetValueAtIndexAutoShrink() throws Exception {
        array = new DynamicBitsizeUnsignedArray(1000, 1, 32, true);
        int[] standardArray = new int[1000];
        Random rand = new Random();
        for (int i = 0; i < array.size(); i++) {
            int random = rand.nextInt(Integer.MAX_VALUE);
            array.setValueAtIndex(i, random);
            standardArray[i] = random;
            assertEquals(array.getValueAtIndex(i), random);
        }

        for (int i = 0; i < array.size(); i++) {
            assertEquals(array.getValueAtIndex(i), standardArray[i]);
        }

        for (int i = array.size() - 1; i >= 0; i--) {
            array.setValueAtIndex(i, 0);
        }
        assertEquals(array.getCurrentBitsPerValue(), 1);
    }

    @Test
    public void testSpeed() throws Exception {
        array = new DynamicBitsizeUnsignedArray(1000, 1, 32, true);
        Random rand = new Random();

        for (int i = 0; i < array.size(); i++) {
            int random = rand.nextInt(Integer.MAX_VALUE);
            array.setValueAtIndex(i, random);
        }
    }

    @Test
    public void testVsStandardSpeed() throws Exception {
        int[] standardArray = new int[1000];
        Random rand = new Random();

        for (int i = 0; i < array.size(); i++) {
            int random = rand.nextInt(Integer.MAX_VALUE);
            standardArray[i] = random;
        }
    }

    @Test
    public void testForceShrinkUnNonAutoShrink() throws Exception {
        array = new DynamicBitsizeUnsignedArray(250);
        testSetValueAtIndex();
        for (int i = 0; i < array.size(); i++) array.setValueAtIndex(i, 0);

        array.forceShrinkAttempt();
        assertEquals(array.getCurrentBitsPerValue(), 1);
    }
}
