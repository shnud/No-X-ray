package com.shnud.noxray.Packets;

import com.shnud.noxray.Packets.PacketEvents.NoXrayPacketEvent;

/**
 * Created by Andrew on 26/12/2013.
 */
public interface IPacketEventWrapperListener {

    void receivePacketEvent(NoXrayPacketEvent event);
}
