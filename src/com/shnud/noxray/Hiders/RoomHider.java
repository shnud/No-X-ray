package com.shnud.noxray.Hiders;

import com.shnud.noxray.Concurrency.BasicExecutor;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketListener;
import com.shnud.noxray.Utilities.XYZ;
import com.shnud.noxray.World.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 26/12/2013.
 */
public class RoomHider implements Listener, PacketEventListener {

    /*
     * Everything to be executed in this class is confined to the same thread that
     * is used by the executor. We haven't used synchronisation for anything so any
     * access of objects from another thread needs to be externally synchronized
     */
    private static final int PLAYER_LOCATION_CHECK_FREQUENCY_TICKS = 20 * 3;
    private final World _world;
    private final MirrorWorld _mirrorWorld;
    private final RoomList _rooms;
    private final BasicExecutor _executor;
    private final ArrayList<PlayerLocation> _playerLocations;

    public RoomHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _mirrorWorld = new MirrorWorld(_world);
        _rooms = new RoomList(_mirrorWorld);
        _playerLocations = new ArrayList<PlayerLocation>();
        _executor = new BasicExecutor();
        _executor.start();

        // Listen for packets so we can filter block changes and map chunks
        PacketListener.addEventListener(this);

        // Add this to the bukkit listeners so we can load and unload chunks
        NoXray.getInstance().getServer().getPluginManager().registerEvents(this, NoXray.getInstance());

        // Start the player location retreiver task that will continue until plugin is disabled
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), new LocationRetreiver(), PLAYER_LOCATION_CHECK_FREQUENCY_TICKS);
    }

    public World getWorld() {
        return _world;
    }

    // Called from main thread
    public void saveAllData() {
        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _rooms.saveRooms();
                _mirrorWorld.saveAllRegions();
            }
        });
    }

    // Called from main thread
    public void hideAtPlayerLocation(final Player player) {
        if(player.getWorld() != _world)
            return;

        player.sendMessage(ChatColor.YELLOW + "Searching...");
        final Location loc = player.getEyeLocation();

        /*
         * We send player messages here from another thread, not sure
         * we should be doing this. If it causes problems we can just
         * schedule them for sync
         */
        _executor.execute(new Runnable() {
            @Override
            public void run() {
                RoomSearcher searcher = new RoomSearcher(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), _world);
                try {
                    List<XYZ> blocks = searcher.getRoomBlocks();
                    if(blocks.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "No blocks were found");
                        return;
                    }

                    boolean multiple = false;
                    // The new room ID we will use for the blocks that are found
                    int roomID = 0;

                    // Here we search the blocks for an already existing ID so we can merge if possible
                    for(XYZ block : blocks) {
                        int blockID = _mirrorWorld.getRoomIDAtBlock(block.x, block.y, block.z);
                        if(blockID != 0) {
                            if(roomID == 0) roomID = blockID;
                            else if(roomID != blockID) multiple = true;
                        }
                    }

                    // If there are multiple IDs then ask the player whether they would like to
                    // merge the blocks with the same roomID
                    if(multiple) {
                        player.sendMessage(ChatColor.YELLOW + "More than one different room was found while searching");
                        player.sendMessage(ChatColor.YELLOW + "Do you want to merge the rooms? (y/n)");

                        //TODO set up some sort of question mechanism here, possibly question objects with a y/n result in the form of callable
                        return;
                    }

                    if(roomID == 0) roomID = _rooms.getUnusedRoomID();
                    int successBlocks = 0;

                    for(XYZ block : blocks) {
                        if(_mirrorWorld.setRoomIDAtBlock(block.x, block.y, block.z, roomID)) successBlocks++;
                    }

                    if(successBlocks == 0)
                        player.sendMessage(ChatColor.RED + "Hiding was unsuccessful, no blocks could be hidden");
                    else
                        player.sendMessage(ChatColor.GREEN + "Hiding successful! " + successBlocks + " blocks were hidden");

                    // TODO Make sure we add that the player has actually seen this room
                    // TODO here so that if the chunk reloads he can see it straight away

                    return;

                } catch (RoomSearcher.ChunkNotLoadedException e) {
                    player.sendMessage(ChatColor.RED + "Search area was too large; is the area properly enclosed?");
                } catch (RoomSearcher.MaxBlocksReachedException e) {
                    player.sendMessage(ChatColor.RED + "Search area was too large; is the area properly enclosed?");
                }
            }
        });
    }

    // Called from main thread
    public void unHideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;
    }

    // Called from main thread
    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkLoad(final ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _mirrorWorld.loadChunk(event.getChunk().getX(), event.getChunk().getZ());
            }
        });
    }

    // Called from main thread
    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkUnload(final ChunkUnloadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _mirrorWorld.unloadChunk(event.getChunk().getX(), event.getChunk().getZ());
            }
        });
    }

    // Called from main thread
    @Override
    public void receivePacketEvent(BasePacketEvent event) {

    }

    public void triggerPlayerHasSeenRoom(Player player, int roomID) {

    }

    private class LocationRetreiver implements Runnable {
        public void run() {
            // Must be ran on primary thread
            if(!Bukkit.isPrimaryThread())
                return;

            synchronized (RoomHider.this) {
                _playerLocations.clear();
                List<Player> players = _world.getPlayers();

                for(Player p : players) {
                    _playerLocations.add(new PlayerLocation(p, p.getLocation()));
                }
            }

            // Now that we have the locations, return control to the room hiding thread
            _executor.execute(new Runnable() {
                @Override
                public void run() {

                    for(PlayerLocation pl : _playerLocations) {
                        int blockX = pl.getLocation().getBlockX();
                        int blockY = pl.getLocation().getBlockY();
                        int blockZ = pl.getLocation().getBlockZ();

                        for(int iX = -3; iX < 4; iX++) {
                            for(int iZ = -3; iZ < 4; iZ++) {
                                // Only check Y blocks at feet and at eye level
                                for(int iY = 0; iY < 2; iY++) {
                                    int roomID = _mirrorWorld.getRoomIDAtBlock(blockX + iX, blockY + iY, blockZ + iZ);
                                    if(roomID != 0)
                                        triggerPlayerHasSeenRoom(pl.getPlayer(), roomID);
                                }
                            }
                        }

                        //TODO stuff that reveals rooms

                    }


                    // Schedule another sync task to update the player locations and do this again
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), new LocationRetreiver(), PLAYER_LOCATION_CHECK_FREQUENCY_TICKS);
                }
            });
        }
    }
}
