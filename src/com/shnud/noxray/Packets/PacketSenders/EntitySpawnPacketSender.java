package com.shnud.noxray.Packets.PacketSenders;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Andrew on 28/12/2013.
 */
public class EntitySpawnPacketSender extends GenericEntityPacketSender {

    public EntitySpawnPacketSender(List<Player> receivers, List<Entity> subjects) {
        super(receivers, subjects);
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    @Override
    public void sendImplementation() {
        for(Entity subject : _subjects) {
            getProtocolManager().updateEntity(subject, _receivers);
        }
    }
}
