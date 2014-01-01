package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 01/01/2014.
 */
public class Misc {

    public static void printByte(byte b) {
        String s = "";

        for (int i = 7; i >= 0; i--) {
            s += 1 & (b >> i);
        }

        System.out.println(s);
    }
}
