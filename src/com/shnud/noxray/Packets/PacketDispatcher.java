package com.shnud.noxray.Packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Lists;
import com.shnud.noxray.Packets.PacketSenders.DestroyEntityPacketSender;
import com.shnud.noxray.Packets.PacketSenders.EntitySpawnPacketSender;
import com.shnud.noxray.Utilities.ArraySplitter;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 26/12/2013.
 */
public class PacketDispatcher {

    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();

    public static void spawnEntityForPlayer(Entity subject, Player receiver) {
        new EntitySpawnPacketSender(Lists.newArrayList(receiver), Lists.newArrayList(subject)).send();
    }

    public static void spawnEntityForPlayers(Entity subject, List<Player> receivers) {
        new EntitySpawnPacketSender(receivers, Lists.newArrayList(subject)).send();
    }

    public static void destroyEntityForPlayer(int entityID, Player receiver) {
        destoryEntitiesForPlayer(new int[] {entityID}, receiver);
    }

    public static void destroyEntityForPlayer(Entity subject, Player receiver) {
        destroyEntityForPlayer(subject.getEntityId(), receiver);
    }

    public static void destoryEntitiesForPlayer(int[] entityIDs, Player receiver) {
        new DestroyEntityPacketSender(Lists.newArrayList(receiver), entityIDs).send();
    }

    public static void resendAllEntitySpawnPacketsForWorld(World world) {
        List<Entity> entities = world.getEntities();

        for(Entity entity : entities) {
            List<Player> trackers = _pm.getEntityTrackers(entity);
            if(!trackers.isEmpty())
                new EntitySpawnPacketSender(trackers, Lists.newArrayList(entity)).send();
        }
    }

    public static void resendAllPlayerSpawnPacketsForWorld(World world) {
        List<Player> players = world.getPlayers();

        //for(Entity entity : players) {
        //   new EntitySpawnPacketSender(players, Lists.newArrayList(entity)).send();
        //}

        for(Entity entity : players) {
            List<Player> trackers = _pm.getEntityTrackers(entity);
            if(!trackers.isEmpty())
                new EntitySpawnPacketSender(trackers, Lists.newArrayList(entity)).send();
        }
    }
}
