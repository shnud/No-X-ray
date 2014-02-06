package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.NoXray;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Created by Andrew on 26/12/2013.
 */
public abstract class NoXrayPacketEvent {
    private PacketEvent _event;

    public NoXrayPacketEvent(PacketEvent event) {
        if(event == null)
            throw new IllegalArgumentException("Event cannot be null");

        _event = event;
    }

    public Player getReceiver() {
        return _event.getPlayer();
    }

    public Object getProcessingLock() {
        return _event.getAsyncMarker().getProcessingLock();
    }

    public boolean isAsync() {
        return _event.isAsync();
    }

    public PacketType getPacketType() {
        return _event.getPacketType();
    }

    /**
     * Forces an asynchronous packet to be sent regardless of how many other threads have
     * said they're waiting to process it
     */
    public void forceSend() {
        if(!isAsync()) {
            NoXray.log(Level.WARNING, "Attempted to force send a sync packet, unnecesaary");
            Thread.dumpStack();
            return;
        }

        while(_event.getAsyncMarker().getProcessingDelay() > 0) {
            decrementAsyncProcessingCountAndSendIfZero();
        }
    }

    public void incrementAsyncProcessingCount() {
        _event.getAsyncMarker().incrementProcessingDelay();
    }

    public void decrementAsyncProcessingCountAndSendIfZero() {
        if(!_event.getAsyncMarker().isTransmitted())
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(_event);
    }

    public void cancel() {
        _event.setCancelled(true);

        if(isAsync())
            forceSend();
    }

    protected PacketContainer getPacket() { return _event.getPacket(); }

    protected ProtocolManager getProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    protected PacketEvent getEvent() {
        return _event;
    }
}
