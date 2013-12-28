package com.shnud.noxray.Entities;

import com.shnud.noxray.Entities.Grouping.EntityCoupleHidable;
import com.shnud.noxray.Entities.Grouping.EntityCoupleList;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.Events.EntityUpdatePacketEvent;
import com.shnud.noxray.Events.PlayerSpawnPacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketDispatcher;
import com.shnud.noxray.Packets.PacketListener;
import com.shnud.noxray.Settings.NoXraySettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import com.shnud.noxray.Packets.PacketEventListener;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

/**
 * Created by Andrew on 23/12/2013.
 */
public class PlayerHider implements PacketEventListener {
    private World _world;
    private EntityCoupleList<EntityCoupleHidable> _couples = new EntityCoupleList<EntityCoupleHidable>();
    private BukkitTask _checkingTask;

    public PlayerHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        resetAndInit();
        PacketListener.addEventListener(this);
    }

    public void resetAndInit() {
        _couples.clear();
        PacketDispatcher.resendAllSpawnPacketsForWorld(_world);

        if(_checkingTask != null)
            _checkingTask.cancel();

        _checkingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                NoXray.getInstance(),
                new CoupleCheckThread(),
                NoXraySettings.PLAYER_TICK_CHECK_FREQUENCY,
                NoXraySettings.PLAYER_TICK_CHECK_FREQUENCY
        );
    }

    @Override
    public void receivePacketEvent(BasePacketEvent event) {
        // Only deal with packets to do with this world
        if(!event.getReceiver().getWorld().equals(_world))
            return;

        else if (event instanceof PlayerSpawnPacketEvent)
            handlePlayerSpawnPacketEvent((PlayerSpawnPacketEvent) event);
        else if (event instanceof EntityUpdatePacketEvent)
            handleEntityUpdatePacketEvent((EntityUpdatePacketEvent) event);
    }

    public void handlePlayerSpawnPacketEvent(PlayerSpawnPacketEvent event) {
        if(_couples.containsCoupleFromEntities(event.getReceiver(), event.getSubject()))
            return;

        EntityCoupleHidable couple = new EntityCoupleHidable(event.getReceiver(), event.getSubject());
        updateCoupleHiddenStatus(couple);

        if(couple.areHidden())
            event.cancel();

        _couples.addCouple(couple);
    }

    public void handleEntityUpdatePacketEvent(EntityUpdatePacketEvent event) {
        if(event.getSubject().getType() != EntityType.PLAYER)
            return;

        if(_couples.getCoupleFromEntities(event.getReceiver(), event.getSubject()).areHidden())
            event.cancel();

        /* Maybe here we could add some sort of flag to the couple to store the last time
         * the server tried to send an entity update packet for the couple. This could then
         * be used instead of ProtocolManager's getEntityTrackers to remove couples from
         * the list which are no longer in need of being tracked
         */
    }

    public void updateCoupleHiddenStatus(EntityCoupleHidable couple) {
        boolean LOS = couple.haveClearLOS();

        if(LOS && couple.areHidden())
            couple.show();
        else if(!LOS && !couple.areHidden())
            couple.hide();
    }

    public class CoupleCheckThread implements Runnable {

        @Override
        public void run() {
            for (EntityCoupleHidable couple : _couples) {
                updateCoupleHiddenStatus(couple);
            }
        }
    }
}