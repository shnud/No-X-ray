package com.shnud.noxray.Rooms;

import java.io.File;

/**
 * Created by Andrew on 22/12/2013.
 */
public class MirrorRegion {

    private int _x, _z;
    private int retainCount;

    public static MirrorRegion constructFromExistingFile(File f, int x, int z) {
        return new MirrorRegion(f, x, z);
    }

    private MirrorRegion(File f, int x, int z) {
        _x = x;
        _z = z;
    }
}
