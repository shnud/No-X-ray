package com.shnud.noxray.Utilities;

import java.util.ArrayList;

/**
 * Created by Andrew on 26/12/2013.
 */
public class ArraySplitter {

    public static Object[][] splitObjectArray(Object[] inputArray, int sectionSize) {
        if(sectionSize <= 0)
            throw new IllegalArgumentException("Section size must be greater than 0");

        boolean perfectlyDivisible = inputArray.length % sectionSize == 0;
        int sections = (inputArray.length / sectionSize) + (perfectlyDivisible ? 1 : 0);
        Object[][] outputArray = new Object[sections][sectionSize];

        for(int s = 0; s < sections; s++) {
            int offset = s * sectionSize;

            for(int i = 0; i < sectionSize; i++) {
                if(offset + i > inputArray.length)
                    break;

                outputArray[s][i] = inputArray[offset + i];
            }
        }

        return outputArray;
    }

    public static int[][] splitIntArray(int[] inputArray, int sectionSize) {
        if(sectionSize <= 0)
            throw new IllegalArgumentException("Section size must be greater than 0");

        boolean perfectlyDivisible = inputArray.length % sectionSize == 0;
        int sections = (inputArray.length / sectionSize) + (perfectlyDivisible ? 1 : 0);
        int[][] outputArray = new int[sections][sectionSize];

        for(int s = 0; s < sections; s++) {
            int offset = s * sectionSize;

            for(int i = 0; i < sectionSize; i++) {
                if(offset + i > inputArray.length)
                    break;

                outputArray[s][i] = inputArray[offset + i];
            }
        }

        return outputArray;
    }
}
