package com.shnud.noxray.Packets.PacketEvents;

import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

public class PlayerSpawnPacketEvent extends NoXrayPacketEvent {

    private final Player _player;

    public PlayerSpawnPacketEvent(PacketEvent event, Player player) {
        super(event);
        _player = player;
    }

    public Player getPlayer() {
        return _player;
    }
}
