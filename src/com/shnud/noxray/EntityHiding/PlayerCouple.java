package com.shnud.noxray.EntityHiding;

import com.shnud.noxray.Utilities.MathHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 23/12/2013.
 */
public class PlayerCouple {
    private Player _player1;
    private Player _player2;

    public PlayerCouple(Player player1, Player player2) {
        if(player1.equals(player2))
            throw new IllegalArgumentException("Entities cannot be the same");

        _player1 = player1;
        _player2 = player2;
    }

    public static long uniqueIDFromEntityPair(Entity e1, Entity e2) {
        int x = e1.getEntityId();
        int y = e2.getEntityId();

        return MathHelper.cantorPair(x, y);
    }

    public long uniqueID() {
        return uniqueIDFromEntityPair(_player1, _player2);
    }

    public int hashCode() {
        return ((Long) uniqueID()).hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof PlayerCouple))
            return false;

        PlayerCouple compare = (PlayerCouple) o;
        boolean player1Shared = compare._player1.equals(_player1) || compare._player1.equals(_player2);
        boolean player2Shared = compare._player2.equals(_player1) || compare._player2.equals(_player2);
        return player1Shared && player2Shared;
    }

    public Player getPlayer1() {
        return _player1;
    }

    public Player getPlayer2() {
        return _player2;
    }
}
