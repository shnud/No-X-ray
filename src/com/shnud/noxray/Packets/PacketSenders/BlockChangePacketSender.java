package com.shnud.noxray.Packets.PacketSenders;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Andrew on 09/01/2014.
 */
public class BlockChangePacketSender extends AbstractPacketSender {
    public BlockChangePacketSender(List<Player> receivers) {
        super(receivers);
    }

    @Override
    protected boolean isThreadSafe() {
        return true;
    }

    @Override
    protected void sendImplementation() {

    }
}
