package com.shnud.noxray.Commands;

import com.google.common.collect.Lists;
import com.shnud.noxray.Hiders.RoomHider;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketSenders.ParticlePacketSender;
import com.shnud.noxray.Utilities.XYZ;
import com.shnud.noxray.World.RoomSearcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Andrew on 03/01/2014.
 */
public class HideCommandTask implements Runnable {
    private static enum TaskStage {
        SEARCH_REAL_WORLD(false),
        CHECK_BLOCKS(true),
        ADD_BLOCKS(true),
        NOTIFY_PLAYER_RESULT(true);
        private final boolean _sync;

        private TaskStage(boolean sync) {
            _sync = sync;
        }

        public boolean isSync() {
            return _sync;
        }
    }

    private static final boolean SEND_PARTICLES = true;
    private static final int PARTICLE_SEND_INTERVAL_BLOCKS = 15;

    private Player _caller;
    private int _x, _y, _z;
    private int _roomID = -1;
    private TaskStage _stage = TaskStage.SEARCH_REAL_WORLD;
    private ArrayList<XYZ> _roomBlocks;
    private String _playerMessage;

    public HideCommandTask(Player caller, int x, int y, int z) {
        _caller = caller;
        _x = z;
        _y = y;
        _z = z;
    }

    public void run() {
        if(reCall(_stage.isSync()))
            return;

        switch(getStage()) {
            case SEARCH_REAL_WORLD:
                searchRealWorld();
                break;
            case CHECK_BLOCKS:
                checkBlocks();
                break;
            case ADD_BLOCKS:
                addBlocks();
                break;
            case NOTIFY_PLAYER_RESULT:
                notifyPlayer();
                break;
        }
    }

    private void searchRealWorld() {
        try {
            Location loc = _caller.getEyeLocation();
            RoomSearcher searcher = new RoomSearcher(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), _caller.getWorld());

            synchronized (this) {
                _roomBlocks = searcher.getRoomBlocks();
            }

            switchStage(TaskStage.CHECK_BLOCKS);
            return;

        } catch (RoomSearcher.ChunkNotLoadedException e) {
            setPlayerMessage("While searching we ventured into unloaded territory");
        } catch (RoomSearcher.MaxBlocksReachedException e) {
            setPlayerMessage("The room was too big and we ran out of time to search, is it totally enclosed?");
        }

        switchStage(TaskStage.NOTIFY_PLAYER_RESULT);
        return;
    }

    public void checkBlocks() {
        if(_roomBlocks == null || _roomBlocks.isEmpty()) {
            setPlayerMessage("Unable to find any blocks");
            notifyPlayer();
            return;
        }

        RoomHider hider = NoXray.getInstance().getRoomHiderForWorld(_caller.getWorld());

        if(hider == null)
            return;

        int foundID = -1;
        boolean foundMultiple = false;

        System.out.println("HELLO");

        Location loc = _caller.getEyeLocation();
        ParticlePacketSender packet = new ParticlePacketSender(Lists.newArrayList(_caller), ParticlePacketSender.ParticleEffect.CRIT, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        int i = 0;

        for(XYZ block : _roomBlocks) {
            int roomID = hider.getRoomIDAtBlock(block.x, block.y, block.z);

            if(i == PARTICLE_SEND_INTERVAL_BLOCKS) {
                packet.setX(block.x);
                packet.setY(block.y);
                packet.setZ(block.z);
                packet.send();
                i = 0;
            }

            if(roomID != 0) {
                if(foundID == -1)
                    foundID = roomID;
                else if(roomID != foundID) {
                    foundMultiple = true;
                    break;
                }
            }

            i++;
        }

        if(foundMultiple) {
            setPlayerMessage(ChatColor.RED + "More than one different room was found, please unhide at least one of these rooms first");
            notifyPlayer();
            return;
        }

        if(foundID == -1) {
            setRoomID(hider.getRooms().getUnusedRoomID());
            notifyPlayer();
            return;
        }
        else {
            setRoomID(foundID);
            addBlocks();
            return;
        }
    }

    private void addBlocks() {
        if(getRoomID() < 1) {
            setPlayerMessage("Unable to create new room ID");
            notifyPlayer();
            return;
        }

        RoomHider hider = NoXray.getInstance().getRoomHiderForWorld(_caller.getWorld());

        if(hider == null)
            return;

        for(XYZ block : _roomBlocks) {
            hider.setBlockToRoomID(block.x, block.y, block.z, getRoomID());
        }

        setPlayerMessage(ChatColor.GREEN + "Successfully protected " + _roomBlocks.size() + " blocks with room ID: " + getRoomID());
        notifyPlayer();
        return;
    }

    private void notifyPlayer() {
        _caller.sendMessage(getPlayerMessage());
    }

    /**
     * Re-call this task asyncronously
     * @return whether the task was scheduled (false if we're already async)
     */
    private boolean switchToAsync() {
        if(!Bukkit.isPrimaryThread())
            return false;

        Bukkit.getScheduler().runTaskAsynchronously(NoXray.getInstance(), this);
        return true;
    }

    /**
     * Re-call this task syncronously
     * @return whether the task was scheduled (false if we're already sync)
     */
    private boolean switchToSync() {
        if(Bukkit.isPrimaryThread())
            return false;

        Bukkit.getScheduler().runTask(NoXray.getInstance(), this);
        return true;
    }

    synchronized private void switchStage(TaskStage stage) {
        _stage = stage;
        reCall(stage.isSync());
    }

    synchronized private TaskStage getStage() {
        return _stage;
    }

    synchronized private void setPlayerMessage(String message) {
        _playerMessage = message;
    }

    synchronized private String getPlayerMessage() {
        return _playerMessage;
    }

    synchronized private void setRoomID(int roomID) {
        _roomID = roomID;
    }

    synchronized private int getRoomID() {
        return _roomID;
    }

    private boolean reCall(boolean sync) {
        if(sync)
            return switchToSync();
        else
            return switchToAsync();
    }
}
