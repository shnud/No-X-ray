package com.shnud.noxray.Packets.PacketAdapters;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEvents.NoXrayPacketEvent;

import java.util.Arrays;

public abstract class NoXrayPacketAdapter extends PacketAdapter {
    private final Iterable<IPacketEventWrapperListener> _listeners;
    private final Iterable<PacketType> _packetTypes;

    public NoXrayPacketAdapter(
            Iterable<IPacketEventWrapperListener> listeners,
            PacketType... types)
    {

        /*
         * We set this priority to highest, so that the destory packets we send
         * from our plugin don't get filtered
         */

        super(NoXray.getInstance(), ListenerPriority.HIGHEST, types);
        _packetTypes = Arrays.asList(types);
        _listeners = listeners;
    }


    protected void dispatchEventToListeners(NoXrayPacketEvent event) {
        for (IPacketEventWrapperListener listener : _listeners) {
            listener.receivePacketEvent(event);
        }
    }

    protected final Iterable<PacketType> getPacketTypes() {
        return _packetTypes;
    }

    @Override
    public final void onPacketSending(PacketEvent event) {
        if(event.isCancelled())
            return;

        packetSendingImplementation(event);
    }

    protected abstract void packetSendingImplementation(PacketEvent event);
}
