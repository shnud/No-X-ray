package com.shnud.noxray.Rooms;

/**
 * Created by Andrew on 30/12/2013.
 */
public interface MirrorChunkEventListener {

    void chunkChangeEvent(int x, int z);

    void roomAddedToChunkEvent(int roomID, int x, int z);

    void roomRemovedFromChunkEvent(int roomID, int x, int z);
}
