package com.shnud.noxray.World;

import java.util.HashMap;

/**
 * Created by Andrew on 02/01/2014.
 */
public class MirrorRegionMap {
    private HashMap<String, MirrorRegion> _regionMap = new HashMap<String, MirrorRegion>();

    public MirrorRegion getRegion(int regionX, int regionZ) {
        String key = keyFromCoordinates(regionX, regionZ);

        return _regionMap.get(key);
    }

    public boolean containsRegion(int regionX, int regionZ) {
        String key = keyFromCoordinates(regionX, regionZ);

        return _regionMap.containsKey(key);
    }

    public void putRegion(MirrorRegion region) {
        String key = keyFromCoordinates(region.getX(), region.getZ());

        _regionMap.put(key, region);
    }

    public void removeRegion(MirrorRegion region) {
        String key = keyFromCoordinates(region.getX(), region.getZ());

        _regionMap.remove(key);
    }

    public void removeRegion(int regionX, int regionZ) {
        String key = keyFromCoordinates(regionX, regionZ);

        _regionMap.remove(key);
    }

    private static String keyFromCoordinates(int x, int z) {
        return x + ":" + z;
    }
}
