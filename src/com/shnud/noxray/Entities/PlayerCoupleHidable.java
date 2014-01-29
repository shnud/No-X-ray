package com.shnud.noxray.Entities;

import com.shnud.noxray.Packets.PacketDispatcher;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by Andrew on 26/12/2013.
 */

@NotThreadSafe
public class PlayerCoupleHidable extends PlayerCouple {

    private static final int MAX_PLAYER_VISIBLE_DISTANCE = 50;
    private double _lastDistance;
    private boolean _areHidden = true;

    public PlayerCoupleHidable(Player player1, Player player2) {
        super(player1, player2);
    }

    /*
     * inititalHiddenStatus is a flag to tell the constructor whether the
     * players in the couple are already hidden from each other. If set to true,
     * the class will not attempt to hide players if they don't have LOS
     * straight away, as it thinks they are already hidden.
     *
     * The only reason for this is in case the plugin is turned on while
     * the server has already been running without the plugin for an amount
     * of time already. In which case, the player's would already be able to
     * see each other and it would be necessary to pass in 'false' so that
     * we have chance to hide them.
     *
     * Otherwise, a 'destroy' packet when the plugin is reloaded could hint
     * to a player that another is nearby. If the plugin was already running
     * before the reload, we can assume that the couple is already hidden from
     * each other (obviously if no LOS). In this case we don't want to send
     * a destroy packet for the reason stated before.
     */
    public PlayerCoupleHidable(Player player1, Player player2, boolean initialHiddenStatus) {
        super(player1, player2);
        _areHidden = initialHiddenStatus;
    }

    public boolean areHidden() {
        return _areHidden;
    }

    public void updateVisibility() {
        boolean LOS = shouldShowEachOther();

        if(LOS && _areHidden)
            show();
        else if(!LOS && !_areHidden)
            hide();
    }

    private boolean shouldShowEachOther() {
        _lastDistance = getDistanceBetween();

        if(_lastDistance < 1)
            return true;
        if(_lastDistance > MAX_PLAYER_VISIBLE_DISTANCE)
            return false;

        return haveClearLOS();
    }

    private double getDistanceBetween() {
        return getPlayer1().getLocation().distance(getPlayer2().getLocation());
    }

    private boolean haveClearLOS() {
        World world = getPlayer1().getWorld();
        Vector start = getPlayer1().getEyeLocation().toVector();
        Vector direction = getPlayer2().getEyeLocation().toVector().subtract(start).normalize();

        BlockIterator blockIterator = new BlockIterator(world, start, direction, 0, (int) _lastDistance);

        while(blockIterator.hasNext()) {
            if(blockIterator.next().getType().isOccluding())
                return false;
        }

        return true;
    }

    private void hide() {
        _areHidden = true;
        if(getPlayer1().isOnline() && getPlayer2().isOnline()) {
            PacketDispatcher.destroyEntityForPlayer(getPlayer2(), getPlayer1());
            PacketDispatcher.destroyEntityForPlayer(getPlayer1(), getPlayer2());
        }
    }

    private void show() {
        _areHidden = false;
        if(getPlayer1().isOnline() && getPlayer2().isOnline()) {
            PacketDispatcher.spawnEntityForPlayer(getPlayer2(), getPlayer1());
            PacketDispatcher.spawnEntityForPlayer(getPlayer1(), getPlayer2());
        }
    }

    public String toString() {
        return "Player 1: " + getPlayer1() + " Player 2: " + getPlayer2() + " Hidden: " + _areHidden;
    }

    public boolean isValid() {
        if(!getPlayer1().isOnline())
            return false;
        if(!getPlayer2().isOnline())
            return false;
        if(!areInSameWorld())
            return false;

        return true;
    }

    private boolean areInSameWorld() {
        return getPlayer1().getWorld().equals(getPlayer2().getWorld());
    }
}
