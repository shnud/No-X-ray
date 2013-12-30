package com.shnud.noxray.Packets.PacketSenders;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Andrew on 30/12/2013.
 */
public class ParticlePacketSender extends PacketSender {

    public static enum ParticleEffect {
        HUGE_EXPLOSION("hugeexplosion"),
        LARGE_EXPLODE("largeexplode"),
        FIREWORKS_SPARK("fireworksSpark"),
        BUBBLE("bubble"),
        SUSPEND("suspend"),
        DEPTH_SUSPEND("depthSuspend"),
        TOWN_AURA("townaura"),
        CRIT("crit"),
        MAGIC_CRIT("magicCrit"),
        MOB_SPELL("mobSpell"),
        MOB_SPELL_AMBIENT("mobSpellAmbient"),
        SPELL("spell"),
        INSTANT_SPELL("instantSpell"),
        WITCH_MAGIC("witchMagic"),
        NOTE("note"),
        PORTAL("portal"),
        ENCHANTMENT_TABLE("enchantmenttable"),
        EXPLODE("explode"),
        FLAME("flame"),
        LAVA("lava"),
        FOOTSTEP("footstep"),
        SPLASH("splash"),
        LARGE_SMOKE("largesmoke"),
        CLOUD("cloud"),
        RED_DUST("reddust"),
        SNOWBALL_POOF("snowballpoof"),
        DRIP_WATER("dripWater"),
        DRIP_LAVA("dripLava"),
        SNOW_SHOVEL("snowshovel"),
        SLIME("slime"),
        HEART("heart"),
        ANGRY_VILLAGER("angryVillager"),
        HAPPY_VILLAGER("happerVillager"),
        ICONCRACK("iconcrack_"),
        TILECRACK("tilecrack_");

        private final String _name;

        private ParticleEffect (final String name) {
            _name = name;
        }

        public String getPacketName() {
            return _name;
        }
    }

    private ParticleEffect _effectType;
    private float _x, _y, _z;

    public ParticlePacketSender(List<Player> receivers, ParticleEffect effectType, float x, float y, float z) {
        super(receivers);

        _effectType = effectType;
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    protected boolean isThreadSafe() {
        return true;
    }

    @Override
    protected void sendImplementation() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);

        packet.getStrings().write(0, _effectType.getPacketName());
        packet.getFloat().write(0, _x); // x-location
        packet.getFloat().write(1, _y); // y-location
        packet.getFloat().write(2, _z); // z-location
        packet.getFloat().write(3, 0f); // x-offset
        packet.getFloat().write(4, 0f); // y-offset
        packet.getFloat().write(5, 0f); // z-offset

        for(Player receiver : _receivers) {
            try {
                getProtocolManager().sendServerPacket(receiver, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
