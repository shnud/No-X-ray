package com.shnud.noxray.Events;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

/**
 * Created by Andrew on 26/12/2013.
 */
public class BasePacketEvent {

    private Player _receiver;
    private PacketEvent _event;

    public BasePacketEvent(Player receiver, PacketEvent event) {
        if(event == null || receiver == null)
            throw new IllegalArgumentException("Event cannot be null");

        _receiver = receiver;
        _event = event;
    }

    public Player getReceiver() {
        return _receiver;
    }

    public void cancel() {
        _event.setCancelled(true);
    }

    public boolean isEventCancelled() {
        return _event.isCancelled();
    }

    public void uncancel() {
        _event.setCancelled(false);
    }
}
