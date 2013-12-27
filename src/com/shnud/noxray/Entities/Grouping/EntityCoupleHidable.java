package com.shnud.noxray.Entities.Grouping;

import com.shnud.noxray.Packets.PacketDispatch;
import com.shnud.noxray.Settings.NoXraySettings;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 * Created by Andrew on 26/12/2013.
 */
public class EntityCoupleHidable extends EntityCouple {

    private double _lastDistance;
    private boolean _distanceInitialised = false;
    private long _timeOfLastCheck = 0;
    private boolean _areHidden = false;

    public EntityCoupleHidable(Entity entity1, Entity entity2) {
        super(entity1, entity2);
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
        _lastDistance = getEntity1().getLocation().distance(getEntity2().getLocation());
        _timeOfLastCheck = System.currentTimeMillis();
        return _lastDistance;
    }

    public boolean haveClearLOS() {
        if(!areInSameWorld())
            return false;

        _lastDistance = getCurrentDistance();

        if(_lastDistance < 1)
            return true;
        if(_lastDistance > NoXraySettings.MAX_PLAYER_VISIBLE_DISTANCE)
            return false;

        World world = getEntity1().getWorld();
        Vector start;
        Vector direction;

        if(getEntity1() instanceof LivingEntity)
            start = ((LivingEntity) getEntity1()).getEyeLocation().toVector();
        else
            start = getEntity1().getLocation().toVector();

        if(getEntity2() instanceof LivingEntity)
            direction = ((LivingEntity) getEntity2()).getEyeLocation().toVector().subtract(start).normalize();
        else
            direction = getEntity2().getLocation().toVector().subtract(start).normalize();

        BlockIterator blockIterator = new BlockIterator(world, start, direction, 0, (int) _lastDistance);

        while(blockIterator.hasNext()) {
            if(blockIterator.next().getType().isOccluding())
                return false;
        }

        return true;
    }

    public boolean areInSameWorld() {
        return getEntity1().getWorld().equals(getEntity2().getWorld());
    }

    public boolean areHidden() {
        return _areHidden;
    }

    public void hide() {
        if(getEntity1() instanceof Player)
            PacketDispatch.destroyEntityForPlayer(getEntity2(), (Player) getEntity1());
        if(getEntity2() instanceof Player)
            PacketDispatch.destroyEntityForPlayer(getEntity1(), (Player) getEntity2());

        _areHidden = true;
    }

    public void show() {
        if(getEntity1() instanceof Player)
            PacketDispatch.spawnEntityForPlayer(getEntity2(), (Player) getEntity1());
        if(getEntity2() instanceof Player)
            PacketDispatch.spawnEntityForPlayer(getEntity1(), (Player) getEntity2());

        _areHidden = false;
    }
}
