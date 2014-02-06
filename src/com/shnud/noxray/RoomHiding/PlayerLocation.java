package com.shnud.noxray.RoomHiding;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;

/**
* Created by Andrew on 07/01/2014.
*/
@Immutable
public class PlayerLocation {
    private final Player _player;
    private final Location _location;

    public PlayerLocation(final Player player, final Location location) {
        _player = player;
        _location = location;
    }

    public Player getPlayer() {
        return _player;
    }

    public Location getLocation() {
        return _location;
    }
}
