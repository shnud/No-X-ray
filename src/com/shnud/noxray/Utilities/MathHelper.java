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
        return ((x + y) * (x + y + 1)) / 2 + y;
    }
}
