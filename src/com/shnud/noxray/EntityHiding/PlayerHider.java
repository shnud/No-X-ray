package com.shnud.noxray.EntityHiding;

import com.shnud.noxray.Entities.PlayerCoupleHidable;
import com.shnud.noxray.Entities.PlayerCoupleList;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketEvents.EntityDestroyPacketEvent;
import com.shnud.noxray.Packets.PacketEvents.EntityUpdatePacketEvent;
import com.shnud.noxray.Packets.PacketEvents.NoXrayPacketEvent;
import com.shnud.noxray.Packets.PacketEvents.PlayerSpawnPacketEvent;
import com.shnud.noxray.Packets.PacketTools;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;

/**
 * Created by Andrew on 23/12/2013.
 */
public class PlayerHider implements IPacketEventWrapperListener {

    private static final int PLAYER_TICK_CHECK_FREQUENCY = MagicValues.MINECRAFT_TICKS_PER_SECOND * 3;
    private final World _world;
    private final PlayerCoupleList<PlayerCoupleHidable> _coupleWatchList = new PlayerCoupleList<PlayerCoupleHidable>();
    private BukkitTask _checkingTask;

    public PlayerHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        PacketEventListener.get().addListener(this);
        PacketTools.resendAllPlayerSpawnPacketsForWorld(_world);
        initiateCoupleCheckingTask();
    }

    @Override
    public void receivePacketEvent(NoXrayPacketEvent event) {
        // Only deal with packets to do with this world
        if(!event.getReceiver().getWorld().equals(_world))
            return;

        else if (event instanceof PlayerSpawnPacketEvent)
            onPlayerSpawnPacketEvent((PlayerSpawnPacketEvent) event);
        else if (event instanceof EntityDestroyPacketEvent)
            onEntityDestroyPacketEvent((EntityDestroyPacketEvent) event);
        else if (event instanceof EntityUpdatePacketEvent)
            onEntityUpdatePacketEvent((EntityUpdatePacketEvent) event);
    }

    private void onEntityDestroyPacketEvent(EntityDestroyPacketEvent event) {
        if(event.getEntity().getType() != EntityType.PLAYER)
            return;

        long id = PlayerCoupleHidable.uniqueIDFromEntityPair(event.getReceiver(), event.getEntity());
        if(_coupleWatchList.containsCouple(id))
            _coupleWatchList.removeCouple(id);
    }

    private void onPlayerSpawnPacketEvent(PlayerSpawnPacketEvent event) {
        // No point adding a couple with the same player, cannot hide from yourself
        if(event.getPlayer().equals(event.getReceiver()))
            return;

        // If we're already monitoring the couple, no need to re-put it to the watch list
        if(_coupleWatchList.containsCouple(event.getReceiver(), event.getPlayer())) {

            if(_coupleWatchList.getCouple(event.getReceiver(), event.getPlayer()).areHidden())
                event.cancel();

            // If the event has gotten this far that means it's already in the couple list and
            // the couple are not hidden, so the spawn packet was probably sent from this function,
            // so we just return and let the event continue.
            return;
        }
        // Always cancel the event so that we can handle the hidden/show status of
        // the player completely through the couple object.
        event.cancel();
        PlayerCoupleHidable couple = new PlayerCoupleHidable(event.getReceiver(), event.getPlayer());

        _coupleWatchList.addCouple(couple);

        // It is absolutely necessary that the couple is added before the hidden status
        // is updated. Otherwise, while updating the couple's status, a new spawning packet
        // could be sent if the couple has LOS, and it will call this function again before
        // it ever adds the couple to the watch list, which will happen over and over.
        couple.updateVisibility();
    }

    private void onEntityUpdatePacketEvent(EntityUpdatePacketEvent event) {
        // We're only interested in player updates
        if(event.getEntity().getType() != EntityType.PLAYER)
            return;

        // If we're currently hiding the couple, then don't send
        // any entity updates as it could give a clue as to whether
        // a player may be nearby
        if(_coupleWatchList.containsCouple(event.getReceiver(), event.getEntity()) && _coupleWatchList.getCouple(event
                .getReceiver(), event.getEntity()).areHidden())
            event.cancel();
    }

    private void initiateCoupleCheckingTask() {
        if(_checkingTask != null)
            _checkingTask.cancel();

        _checkingTask = Bukkit.getScheduler().runTaskTimer(

                NoXray.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        Iterator<PlayerCoupleHidable> it = _coupleWatchList.iterator();

                        while(it.hasNext()) {
                            PlayerCoupleHidable couple = it.next();

                            if(couple.isValid())
                                couple.updateVisibility();
                            else
                                it.remove();
                        }
                    }
                },
                PLAYER_TICK_CHECK_FREQUENCY,
                PLAYER_TICK_CHECK_FREQUENCY

        );
    }
}