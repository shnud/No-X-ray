package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class XYZ {
    public int x, y, z;

    public XYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int hashCode() {
        return new String(x + ":" + y + ":" + z).hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof XYZ))
            return false;

        XYZ comp = (XYZ) o;
        return comp.hashCode() == this.hashCode();
    }
}
