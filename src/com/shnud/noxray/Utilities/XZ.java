package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class XZ {
    public final int x, z;

    public XZ(int x, int z) {
        this.x = x;
        this.z = z;
    }

    private int _hashCode = -1;
    private boolean _hashCodeCached = false;

    public int hashCode() {
        if(!_hashCodeCached) {
            _hashCode = new String(x + ":" + z).hashCode();
            _hashCodeCached = true;
        }
        return _hashCode;
    }

    public boolean equals(Object o) {
        if(!(o instanceof XZ))
            return false;

        XZ comp = (XZ) o;
        return comp.x == this.x && comp.z == this.z;
    }
}
