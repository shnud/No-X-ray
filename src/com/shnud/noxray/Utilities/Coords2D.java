package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 22/12/2013.
 */
public class Coords2D {
    public int x, z;

    public Coords2D(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int hashCode() {
        return new String(x + ":" + z).hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof Coords2D))
            return false;

        Coords2D comp = (Coords2D) o;
        return comp.hashCode() == this.hashCode();
    }
}
