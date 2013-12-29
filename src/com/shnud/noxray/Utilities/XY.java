package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class XY {
    public int x, z;

    public XY(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int hashCode() {
        return new String(x + ":" + z).hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof XY))
            return false;

        XY comp = (XY) o;
        return comp.hashCode() == this.hashCode();
    }
}
