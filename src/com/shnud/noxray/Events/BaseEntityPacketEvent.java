package com.shnud.noxray.Events;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 26/12/2013.
 */
public abstract class BaseEntityPacketEvent extends BasePacketEvent {

    private Entity _subject;

    public BaseEntityPacketEvent(Player receiver, Entity subject, PacketEvent event) {
         super(receiver, event);
        _subject = subject;

    }

    public Entity getSubject() {
        return _subject;
    }
}
