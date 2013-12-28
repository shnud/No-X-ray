package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 23/12/2013.
 */
public class MathHelper {

    public static long pow(int val, int exp) {
        long result = val;

        while(exp > 0) {
            result *= val;
            exp--;
        }

        return result;
    }

    public static long cantorPair(long x, long y) {
        /*
         * Always put the smallest number first
         */

        if(x > y) {
            long temp = x;
            x = y;
            y = temp;
        }

        return ((x + y) * (x + y + 1)) / 2 + y;
    }
}
