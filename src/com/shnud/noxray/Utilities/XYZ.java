package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class XYZ {
    public final int x, y, z;

    public XYZ(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private int _hashCode = -1;
    private boolean _hashCodeCached = false;

    public int hashCode() {
        if(!_hashCodeCached) {
            _hashCode = new String(x + ":" + y + ":" + z).hashCode();
            _hashCodeCached = true;
        }
        return _hashCode;
    }

    public boolean equals(Object o) {
        if(!(o instanceof XYZ))
            return false;

        XYZ comp = (XYZ) o;
        return comp.hashCode() == this.hashCode();
    }
}
