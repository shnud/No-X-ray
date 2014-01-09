package com.shnud.noxray.Hiders;

import com.google.common.collect.Lists;
import com.shnud.noxray.Entities.EntityWatcherEntry;
import com.shnud.noxray.Entities.EntityWatcherList;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.Events.EntityDestroyPacketEvent;
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
import java.util.Iterator;

/**
 * Created by Andrew on 27/12/2013.
 */
public class EntityHider implements PacketEventListener {

    private static final int MAXIMUM_Y_FOR_HIDING_NON_PLAYER_ENTITIES = 60;
    private static final int MINIMUM_XZ_DISTANCE_FOR_VISIBLE_ENTITIES = 20;
    private static final int MINIMUM_Y_DISTANCE_FOR_VISIBLE_ENTITIES = 8;
    private static final int ENTITY_TICK_CHECK_FREQUENCY = MagicValues.MINECRAFT_TICKS_PER_SECOND * 4;
    private static final int ENTITY_VISIBILITY_CHECKS_PER_PURGE = 20;

    private final World _world;
    private final EntityWatcherList _entityList = new EntityWatcherList();
    private int checkCount = 0;

    public EntityHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        PacketListener.addEventListener(this);

        // Make sure that if the server has reloaded we try to respawn
        // all of the entities again because we may have been hiding entities
        // before that have now disappeard off the list and players could
        // be hit by invisible enemies
        PacketDispatcher.resendAllEntitySpawnPacketsForWorld(world);

        // All tasks get cancelled on plugin disable so we don't need to
        // worry about stopping this
        Bukkit.getScheduler().runTaskTimer(
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
        else if (event instanceof EntityDestroyPacketEvent)
            handleEntityDestroyPacketEvent((EntityDestroyPacketEvent) event);
        else if (event instanceof EntityUpdatePacketEvent)
            handleEntityUpdatePacketEvent((EntityUpdatePacketEvent) event);
    }

    public void checkEntities() {
        checkCount++;
        ArrayList<Player> playersToSendCurrentEntity = new ArrayList<Player>();

        Iterator<EntityWatcherEntry> entityIterator = _entityList.iterator();

        while(entityIterator.hasNext()) {
            playersToSendCurrentEntity.clear();
            EntityWatcherEntry entry = entityIterator.next();

            Entity entity = entry.getEntity();
            Iterator<Player> watcherIterator = entry.getWatcherIterator();

            while(watcherIterator.hasNext()) {
                Player watcher = watcherIterator.next();

                if(shouldShowEntityToWatcher(entity, watcher)) {
                    watcherIterator.remove();
                    playersToSendCurrentEntity.add(watcher);
                }
            }

            if(!playersToSendCurrentEntity.isEmpty())
                PacketDispatcher.spawnEntityForPlayers(entity, Lists.newArrayList(playersToSendCurrentEntity));

            if(entry.numberOfWatchers() == 0)
                entityIterator.remove();
        }

        if(checkCount < ENTITY_VISIBILITY_CHECKS_PER_PURGE)
            return;

        _entityList.purgeList();
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

        if(!shouldShowEntityToWatcher(event.getSubject(), event.getReceiver())) {
            event.cancel();
            _entityList.addWatcherToEntity(event.getReceiver(), event.getSubject());
        }
    }

    private void handleEntityDestroyPacketEvent(EntityDestroyPacketEvent event) {
        _entityList.removeWatcherFromEntity(event.getReceiver(), event.getSubject());
    }

    private void handleEntityUpdatePacketEvent(EntityUpdatePacketEvent event) {
        if(event.getSubject().getType() == EntityType.PLAYER)
            return;

        if(_entityList.doesEntityHaveWatcher(event.getSubject(), event.getReceiver())) {
            event.cancel();
        }
    }

    private class WatcherCheckThread implements Runnable {

        @Override
        public void run() {
            checkEntities();
        }
    }
}
