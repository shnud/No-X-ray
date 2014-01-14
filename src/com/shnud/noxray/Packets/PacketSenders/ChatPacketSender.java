package com.shnud.noxray.Packets.PacketSenders;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Andrew on 11/01/2014.
 */
public class ChatPacketSender extends AbstractPacketSender {

    private final String _message;

    public ChatPacketSender(List<Player> receivers, String message) {
        super(receivers);
        if(message == null)
            throw new IllegalArgumentException("Message cannot be null");

        _message = message;
    }

    public ChatPacketSender(Player receiver, String message) {
        this(Lists.newArrayList(receiver), message);
    }

    @Override
    protected boolean isThreadSafe() {
        return false;
    }

    @Override
    protected void sendImplementation() {
        for(Player p : _receivers) {
            if(p != null && p.isOnline())
                p.sendMessage(_message);
        }
    }
}
