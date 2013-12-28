package com.shnud.noxray.Entities;

import com.shnud.noxray.Entities.Grouping.EntityWatcherList;
import com.shnud.noxray.Entities.Grouping.EntityWatcherListList;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.Events.EntitySpawnPacketEvent;
import com.shnud.noxray.Events.EntityUpdatePacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketDispatcher;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketListener;
import com.shnud.noxray.Settings.NoXraySettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityHider implements PacketEventListener {

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
                NoXraySettings.ENTITY_TICK_CHECK_FREQUENCY,
                NoXraySettings.ENTITY_TICK_CHECK_FREQUENCY
        );
    }

    @Override
    public void receivePacketEvent(BasePacketEvent event) {
        // Only deal with packets to do with this world
        if(!event.getReceiver().getWorld().equals(_world))
            return;

        else if (event instanceof EntitySpawnPacketEvent)
            handleMobSpawnPacketEvent((EntitySpawnPacketEvent) event);
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


        if(checkCount < NoXraySettings.ENTITY_VISIBILITY_CHECKS_PER_PURGE)
            return;

        _watcherListList.purgeList();
        checkCount = 0;
    }

    public boolean shouldShowEntityToWatcher(Entity subject, Player watcher) {
        if(subject.getLocation().getBlockY() > NoXraySettings.MAXIMUM_Y_FOR_HIDING_NON_PLAYER_ENTITIES)
            return true;

        if(!NoXraySettings.getEntitiesToHide().contains(subject.getType()))
            return true;

        Vector receiverLoc = watcher.getLocation().toVector();
        Vector spawningLoc = subject.getLocation().toVector();
        double yDifference = Math.abs(receiverLoc.getY() - spawningLoc.getY());

        receiverLoc.setY(spawningLoc.getY());
        double xzDifference = receiverLoc.distance(spawningLoc);

        if(xzDifference > NoXraySettings.MINIMUM_XZ_DISTANCE_FOR_VISIBLE_ENTITIES
                || yDifference > NoXraySettings.MINIMUM_Y_DISTANCE_FOR_VISIBLE_ENTITIES) {

            return false;
        }

        return true;
    }

    private void handleMobSpawnPacketEvent(EntitySpawnPacketEvent event) {
        if(shouldShowEntityToWatcher(event.getSubject(), event.getReceiver()))
            return;
        else {
            event.cancel();
            _watcherListList.addWatcher(event.getSubject(), event.getReceiver());
        }
    }

    private void handleEntityUpdatePacketEvent(EntityUpdatePacketEvent event) {

    }

    private class WatcherCheckThread implements Runnable {

        @Override
        public void run() {
            checkWatchers();
        }
    }
}
