package com.shnud.noxray.Hiders;

import com.shnud.noxray.Entities.EntityWatcherList;
import com.shnud.noxray.Entities.EntityWatcherListList;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.Events.EntitySpawnPacketEvent;
import com.shnud.noxray.Events.EntityUpdatePacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketDispatcher;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketListener;
import com.shnud.noxray.Settings.NoXraySettings;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityHider implements PacketEventListener {

    private static final int MAXIMUM_Y_FOR_HIDING_NON_PLAYER_ENTITIES = 60;
    private static final int MINIMUM_XZ_DISTANCE_FOR_VISIBLE_ENTITIES = 20;
    private static final int MINIMUM_Y_DISTANCE_FOR_VISIBLE_ENTITIES = 8;
    private static final int ENTITY_TICK_CHECK_FREQUENCY = MagicValues.MINECRAFT_TICKS_PER_SECOND * 4;
    private static final int ENTITY_VISIBILITY_CHECKS_PER_PURGE = 10;

    private World _world;
    private EntityWatcherListList _watcherListList = new EntityWatcherListList();
    private int _checkingTask = -1;
    private int checkCount = 0;

    public EntityHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        PacketListener.addEventListener(this);

        if(_checkingTask > 0)
            Bukkit.getScheduler().cancelTask(_checkingTask);

        _checkingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                NoXray.getInstance(),
                new WatcherCheckThread(),
                ENTITY_TICK_CHECK_FREQUENCY,
                ENTITY_TICK_CHECK_FREQUENCY
        );
    }

    @Override
    public void receivePacketEvent(BasePacketEvent event) {
        // Only deal with packets to do with this world
        if(!event.getReceiver().getWorld().equals(_world))
            return;

        else if (event instanceof EntitySpawnPacketEvent)
            handleEntitySpawnPacketEvent((EntitySpawnPacketEvent) event);
        else if (event instanceof EntityUpdatePacketEvent)
            handleEntityUpdatePacketEvent((EntityUpdatePacketEvent) event);
    }

    public void checkWatchers() {
        checkCount++;
        ArrayList<Player> playersToSendCurrentEntity = new ArrayList<Player>();

        for(EntityWatcherList watcherList : _watcherListList) {
            Entity e = watcherList.getSubject();
            playersToSendCurrentEntity.clear();

            for(Player p : watcherList) {
                if(shouldShowEntityToWatcher(e, p)) {
                    watcherList.removeWatcher(p);
                    playersToSendCurrentEntity.add(p);
                }
            }

            if(!playersToSendCurrentEntity.isEmpty())
                PacketDispatcher.spawnEntityForPlayers(e, playersToSendCurrentEntity);
        }


        if(checkCount < ENTITY_VISIBILITY_CHECKS_PER_PURGE)
            return;

        _watcherListList.purgeList();
        checkCount = 0;
    }

    public boolean shouldShowEntityToWatcher(Entity subject, Player watcher) {
        if(subject.getLocation().getBlockY() > MAXIMUM_Y_FOR_HIDING_NON_PLAYER_ENTITIES)
            return true;

        if(!NoXraySettings.getHiddenEntities().contains(subject.getType()))
            return true;

        Vector receiverLoc = watcher.getLocation().toVector();
        Vector spawningLoc = subject.getLocation().toVector();
        double yDifference = Math.abs(receiverLoc.getY() - spawningLoc.getY());

        receiverLoc.setY(spawningLoc.getY());
        double xzDifference = receiverLoc.distance(spawningLoc);

        if(xzDifference > MINIMUM_XZ_DISTANCE_FOR_VISIBLE_ENTITIES
                || yDifference > MINIMUM_Y_DISTANCE_FOR_VISIBLE_ENTITIES) {

            return false;
        }

        return true;
    }

    private void handleEntitySpawnPacketEvent(EntitySpawnPacketEvent event) {
        if(event.getSubject().getType() == EntityType.PLAYER)
            return;

        if(shouldShowEntityToWatcher(event.getSubject(), event.getReceiver()))
            return;

        else {
            event.cancel();
            _watcherListList.addWatcher(event.getSubject(), event.getReceiver());
        }
    }

    private void handleEntityUpdatePacketEvent(EntityUpdatePacketEvent event) {
        if(event.getSubject().getType() == EntityType.PLAYER)
            return;

        if(_watcherListList.isEntityBeingHiddenFrom(event.getSubject(), event.getReceiver()))
            event.cancel();
    }

    private class WatcherCheckThread implements Runnable {

        @Override
        public void run() {
            checkWatchers();
        }
    }
}
