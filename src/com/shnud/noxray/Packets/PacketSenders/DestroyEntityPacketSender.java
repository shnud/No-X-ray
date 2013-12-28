package com.shnud.noxray.Packets.PacketSenders;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.shnud.noxray.Utilities.ArraySplitter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Andrew on 28/12/2013.
 */
public class DestroyEntityPacketSender extends PacketSender {

    private static final int DESTORY_ENTITY_PACKET_SPLIT_SIZE = 16;
    private int[] _entityIDs;

    /*
     * Uses entity IDs in the constructor rather than entity objects
     * because the entities may no longer be valid, hence the reason
     * clients are being sent a destory entity packet
     */
    public DestroyEntityPacketSender(List<Player> receivers, int[] entityIDs) {
        super(receivers);

        if(entityIDs == null || entityIDs.length == 0)
            throw new IllegalArgumentException("List of entity IDs cannot be null or empty");

        _entityIDs = entityIDs;
    }

    @Override
    protected boolean isThreadSafe() {
        return true;
    }

    @Override
    protected void sendImplementation() {
        int[][] split;

        if(_entityIDs.length > DESTORY_ENTITY_PACKET_SPLIT_SIZE)
            split = ArraySplitter.splitIntArray(_entityIDs, DESTORY_ENTITY_PACKET_SPLIT_SIZE);
        else
            split = new int[][] {_entityIDs};

        for(int[] array : split) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, array);

            for(Player receiver : _receivers) {
                try {
                    getProtocolManager().sendServerPacket(receiver, packet, false);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
