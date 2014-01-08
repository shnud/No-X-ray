package com.shnud.noxray.World;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Andrew on 08/01/2014.
 */
public class PlayerSeenRooms {

    private final HashMap<String, HashSet<Integer>> _playerRooms = new HashMap<String, HashSet<Integer>>();

    public void addVisibleRoomToPlayer(int roomID, Player player) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null");

        if(roomID < 1)
            throw new IllegalArgumentException("Room ID must be greater than 0");

        if(!_playerRooms.containsKey(player.getName()))
            _playerRooms.put(player.getName(), new HashSet<Integer>());

        _playerRooms.get(player.getName()).add(roomID);
    }

    public void removeVisibleRoomFromPlayer(int roomID, Player player) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null");

        if(roomID < 1)
            throw new IllegalArgumentException("Room ID must be greater than 0");

        if(!_playerRooms.containsKey(player.getName()))
            return;

        _playerRooms.get(player.getName()).remove(roomID);
    }

    public boolean isRoomVisibleForPlayer(int roomID, Player player) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null");

        if(roomID < 1)
            throw new IllegalArgumentException("Room ID must be greater than 0");

        if(!_playerRooms.containsKey(player.getName()))
            return false;

        return _playerRooms.get(player.getName()).contains(roomID);
    }

    public HashSet<Integer> getVisibleRoomsForPlayer(Player player) {
        return _playerRooms.get(player.getName());
    }
}
