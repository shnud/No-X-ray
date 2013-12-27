package com.shnud.noxray.Packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
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
public class PacketDispatch {

    private static final int DESTORY_ENTITY_PACKET_SPLIT_SIZE = 16;
    private static ProtocolManager _pm = ProtocolLibrary.getProtocolManager();

    public static void spawnEntityForPlayer(Entity subject, Player receiver) {
        List players = new ArrayList();
        players.add(receiver);
        _pm.updateEntity(subject, players);
    }

    public static void spawnEntityForPlayers(Entity subject, List<Player> players) {
        _pm.updateEntity(subject, players);
    }

    public static void destroyEntityForPlayer(int entityID, Player receiver) {
        destoryEntitiesForPlayer(new int[] {entityID}, receiver);
    }

    public static void destroyEntityForPlayer(Entity subject, Player receiver) {
        destroyEntityForPlayer(subject.getEntityId(), receiver);
    }

    public static void destoryEntitiesForPlayer(int[] entityIDs, Player receiver) {
        if(entityIDs.length > DESTORY_ENTITY_PACKET_SPLIT_SIZE) {
            int[][] split = ArraySplitter.splitIntArray(entityIDs, DESTORY_ENTITY_PACKET_SPLIT_SIZE);

            for (int[] listOfEntities : split) {
                destoryEntitiesForPlayer(listOfEntities, receiver);
            }
        }
        else {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, entityIDs);

            try {
                _pm.sendServerPacket(receiver, packet, false);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void resendAllSpawnPacketsForWorld(World world) {
        List<Entity> entities = world.getEntities();

        for(Entity entity : entities) {
            List<Player> players = _pm.getEntityTrackers(entity);
            _pm.updateEntity(entity, players);
        }
    }
}
