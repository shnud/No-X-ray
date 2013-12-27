package com.shnud.noxray.Packets;

import com.shnud.noxray.Events.BasePacketEvent;

/**
 * Created by Andrew on 26/12/2013.
 */
public interface PacketEventListener {

    void receivePacketEvent(BasePacketEvent event);
}
