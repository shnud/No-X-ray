package com.shnud.noxray.Entities;

import com.shnud.noxray.Packets.PacketDispatcher;
import com.shnud.noxray.Settings.NoXraySettings;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * Created by Andrew on 26/12/2013.
 */
public class PlayerCoupleHidable extends PlayerCouple {

    private static final int MAX_PLAYER_VISIBLE_DISTANCE = 50;
    private double _lastDistance;
    private boolean _distanceInitialised = false;
    private boolean _areHidden = true;
    private boolean _initalDecision = true;

    public PlayerCoupleHidable(Player player1, Player player2) {
        super(player1, player2);
    }

    /*
     * initHidden is a flag to tell the constructor whether the players
     * in the couple are already hidden from each other. If set to true,
     * the class will not attempt to hide players if they don't have LOS
     * straight away, as it thinks they are already hidden.
     */
    public PlayerCoupleHidable(Player player1, Player player2, boolean initHidden) {
        super(player1, player2);
        _areHidden = initHidden;
    }

    public double getLastDistance() {
        if(!_distanceInitialised)
            throw new IllegalStateException("Distance has not yet been checked");

        return _lastDistance;
    }

    public double getCurrentDistance() {
        if(!areInSameWorld())
            throw new IllegalStateException("Impossible to get distance as players are in seperate worlds");

        _distanceInitialised = true;
        _lastDistance = getPlayer1().getLocation().distance(getPlayer2().getLocation());
        return _lastDistance;
    }

    public boolean haveClearLOS() {
        if(!areInSameWorld())
            return false;

        if(getPlayer1().isDead() || getPlayer2().isDead())
            throw new IllegalArgumentException("Cannot check LOS when one entity is dead");

        if(!getPlayer1().isOnline())
            throw new IllegalArgumentException("Cannot check LOS when one player is offline");
        if(!getPlayer2().isOnline())
            throw new IllegalArgumentException("Cannot check LOS when one player is offline");

        _lastDistance = getCurrentDistance();

        if(_lastDistance < 1)
            return true;
        if(_lastDistance > MAX_PLAYER_VISIBLE_DISTANCE)
            return false;

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

    public boolean areInSameWorld() {
        return getPlayer1().getWorld().equals(getPlayer2().getWorld());
    }

    public boolean areHidden() {
        return _areHidden;
    }

    public void hide() {
        _areHidden = true;
        _initalDecision = false;
        if(getPlayer1().isOnline() && getPlayer2().isOnline()) {
            PacketDispatcher.destroyEntityForPlayer(getPlayer2(), getPlayer1());
            PacketDispatcher.destroyEntityForPlayer(getPlayer1(), getPlayer2());
        }
    }

    public void show() {
        _areHidden = false;
        _initalDecision = false;
        if(getPlayer1().isOnline() && getPlayer2().isOnline()) {
            PacketDispatcher.spawnEntityForPlayer(getPlayer2(), getPlayer1());
            PacketDispatcher.spawnEntityForPlayer(getPlayer1(), getPlayer2());
        }
    }

    /*
     * Used to determine whether the Players are really watching each
     * other as far as the Minecraft server is concered. If they aren't
     * then there's no point keeping this PlayerCouple in the
     * PlayerHider's couple list.
     */
    public boolean areReallyWatching() {
        if(!areInSameWorld())
            return false;

        if(!getPlayer1().isOnline())
            return false;
        if(!getPlayer2().isOnline())
            return false;

        return true;
    }

    public String toString() {
        return "Player 1: " + getPlayer1() + " Player 2: " + getPlayer2() + " Hidden: " + areHidden();
    }

    public boolean isNewCouple() {
        return _initalDecision;
    }
}
