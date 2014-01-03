package com.shnud.noxray.Hiders;

import com.shnud.noxray.Commands.HideCommandTask;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.XYZ;
import com.shnud.noxray.World.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Andrew on 26/12/2013.
 */
public class RoomHider {

    private World _world;
    private MirrorWorld _mirrorWorld;
    private RoomList _rooms;

    public RoomHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _mirrorWorld = new MirrorWorld(_world);
        _rooms = new RoomList(_mirrorWorld);
    }

    public World getWorld() {
        return _world;
    }

    public void saveAllData() {
        _rooms.saveRooms();
        _mirrorWorld.saveAllRegions();
    }

    public void hideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;

        Location loc = player.getEyeLocation();
        new HideCommandTask(player, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).run();
    }

    public void unHideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;
    }

    public void setBlockToRoomID(int x, int y, int z, int roomID) {
        setBlockToRoomID(DynamicCoordinates.initWithBlockCoordinates(x, y, z), roomID);
    }

    public boolean setBlockToRoomID(DynamicCoordinates coordinates, int roomID) {
        MirrorChunk chunk = _mirrorWorld.getChunk(coordinates);
        if(!chunk.isEmpty() && !chunk.containsRoomID(roomID) && chunk.isFull())
            chunk.cleanUp();

        return chunk.setBlockToRoomID(coordinates, roomID);
    }

    public void unHideBlock(int x, int y, int z) {
        setBlockToRoomID(x, y, z, 0);
    }

    public int getRoomIDAtBlock(int x, int y, int z) {
        DynamicCoordinates coordinates = DynamicCoordinates.initWithBlockCoordinates(x, y, z);
        return _mirrorWorld.getChunk(coordinates).getRoomIDAtBlock(coordinates);
    }

    public int getRoomIDAtPlayerLocation(Player player) {
        Location loc = player.getLocation();
        return getRoomIDAtBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public RoomList getRooms() {
        return _rooms;
    }
}
