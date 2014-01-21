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
    protected boolean isThreadSafe() {
        return false;
    }

    @Override
    protected void sendImplementation() {
        for(Entity subject : _subjects) {
            if(subject != null && !subject.isDead() && subject.isValid())
                getProtocolManager().updateEntity(subject, _receivers);
        }
    }
}
