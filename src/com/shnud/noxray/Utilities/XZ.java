package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class XZ {
    public int x, z;

    public XZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int hashCode() {
        return new String(x + ":" + z).hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof XZ))
            return false;

        XZ comp = (XZ) o;
        return comp.hashCode() == this.hashCode();
    }
}
