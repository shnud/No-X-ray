package com.shnud.noxray.Hiders;

import com.comphenix.protocol.ProtocolLibrary;
import com.shnud.noxray.Concurrency.BasicExecutor;
import com.shnud.noxray.Events.BasePacketEvent;
import com.shnud.noxray.Events.MapChunkPacketEvent;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketListener;
import com.shnud.noxray.Packets.PacketSenders.BlockChangePacketSender;
import com.shnud.noxray.Packets.PacketSenders.ChatPacketSender;
import com.shnud.noxray.Packets.PacketSenders.ParticlePacketSender;
import com.shnud.noxray.Settings.PlayerMetadataEntry;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import com.shnud.noxray.Utilities.XYZ;
import com.shnud.noxray.Utilities.XZ;
import com.shnud.noxray.World.*;
import com.shnud.noxray.World.MapBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;

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
    private final ArrayList<PlayerLocation> _playerLocations = new ArrayList<PlayerLocation>();
    private final PlayerSeenRooms _playerRooms = new PlayerSeenRooms();
    private final Material _censorBlock;

    public RoomHider(World world) {
        if(world == null)
            throw new IllegalArgumentException("World cannot be null");

        _world = world;
        _mirrorWorld = new MirrorWorld(_world);

        switch(_world.getEnvironment()) {
            case NORMAL:
                _censorBlock = Material.STONE;
                break;
            case NETHER:
                _censorBlock = Material.NETHERRACK;
                break;
            case THE_END:
                _censorBlock = Material.ENDER_STONE;
                break;
            default:
                _censorBlock = Material.STONE;
        }

        _rooms = new RoomList(_mirrorWorld);
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
    public void disable() {
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

        // Should probably make sure we don't add any new hidden blocks while blocks
        // are being retreived to send to players who have just seen rooms
        // Not the biggest of issues, though

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
                        new ChatPacketSender(player, ChatColor.YELLOW + "More than one different room was found " +
                                "while searching").send();
                        new ChatPacketSender(player, ChatColor.YELLOW + "Do you want to merge the rooms? (y/n)").send();

                        //TODO set up some sort of question mechanism here, possibly question objects with a y/n result in the form of callable
                        return;
                    }

                    if(roomID == 0) roomID = _rooms.getUnusedRoomID();
                    int successBlocks = 0;

                    for(XYZ block : blocks) {
                        if(_mirrorWorld.setRoomIDAtBlock(block.x, block.y, block.z, roomID)) {
                            successBlocks++;
                            _rooms.addKnownChunkToRoom(block.x >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, block.z >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, roomID);
                        }
                    }

                    if(successBlocks == 0)
                        new ChatPacketSender(player, ChatColor.RED + "Hiding was unsuccessful, " +
                                "no blocks could be hidden").send();
                    else {
                        new ChatPacketSender(player, ChatColor.GREEN + "Hiding successful! " + successBlocks + " blocks were hidden").send();

                        // Make sure we add that the player has actually seen this room
                        // here so that if the chunk reloads he can see it straight away

                        _playerRooms.addVisibleRoomToPlayer(roomID, player);
                    }

                    return;

                } catch (RoomSearcher.ChunkNotLoadedException e) {
                    new ChatPacketSender(player, ChatColor.RED + "Search area was too large; is the area properly enclosed?").send();
                } catch (RoomSearcher.MaxBlocksReachedException e) {
                    new ChatPacketSender(player, ChatColor.RED + "Search area was too large; is the area properly enclosed?").send();
                }
            }
        });

    }


    // Called from main thread
    public void unHideAtPlayerLocation(Player player) {
        if(player.getWorld() != _world)
            return;
    }

    // Called from async but not this thread
    @Override
    public void receivePacketEvent(BasePacketEvent event) {
        // We don't care about the event if it's not regarding this world
        if(event.getReceiver().getWorld() != _world)
            return;

        if(event instanceof MapChunkPacketEvent)
            handleMapChunkPacketEvent((MapChunkPacketEvent) event);
    }

    // Called from main thread
    public void handleMapChunkPacketEvent(final MapChunkPacketEvent event) {

        // Before we hand over to our room hiding thread, make sure we increment the
        // processing delay so that it doesn't send as soon as it returns
        event.getPacketEvent().getAsyncMarker().incrementProcessingDelay();

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (event.getPacketEvent().getAsyncMarker().getProcessingLock()) {
                    for (int chunkI = 0; chunkI < event.getAmountOfChunks(); chunkI++) {
                        MapChunkData chunk = event.getChunk(chunkI);
                        DynamicCoordinates coords = DynamicCoordinates.initWithChunkCoordinates(chunk.getX(), 0, chunk.getZ());

                        // If the mirror chunk isn't loaded then it means it was empty so don't bother filtering this chunk. If
                        // we try and get it the mirror world will create a new blank one which we don't want.
                        if (!_mirrorWorld.isMirrorChunkLoaded(coords))
                            continue;

                        MirrorChunk mirror = _mirrorWorld.getMirrorChunk(coords);

                        // It could still be possible that the chunk is empty even if it's loaded, and if that's the case
                        // we don't want to bother going through the whole chunk if there's nothing to hide in it
                        HashSet<Integer> seenRooms = _playerRooms.getVisibleRoomsForPlayer(event.getReceiver());

                        // No return value as it works on the actual chunk, saves copying stuff
                        ChunkCensor.censorChunk(chunk, mirror, seenRooms, _censorBlock);
                    }

                    // Ensure that we recompress the modified data as that's what gets sent to the client
                    event.compressDataForSendingToClient();

                    // Because we incremented the processing delay we now have to signal that we want the packet to be sent
                    ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(event.getPacketEvent());
                }
            }
        });
    }

    public void triggerPlayerHasSeenRoom(final Player player, final int roomID) {
        // If the player can already see this room then there's no
        // need to send him the uncensored blocks again
        if(_playerRooms.isRoomVisibleForPlayer(roomID, player))
            return;

        final Object syncLock = new Object();

        // Use syncronization here on an object that we will also send to the sync thread
        // when getting the real block IDs so that we can ensure that all the blocks we add
        // here are visible to the sync thread
        synchronized (syncLock) {
            HashSet<XZ> chunks = _rooms.getRoomFromID(roomID).getKnownChunks();
            final HashMap<XZ, List<XYZ>> blocksForEachChunk = new HashMap<XZ, List<XYZ>>();

            for(XZ chunk : chunks) {
                MirrorChunk mirChunk = _mirrorWorld.getMirrorChunk(chunk.x, chunk.z);
                if(mirChunk == null)
                    continue;

                List<XYZ> blocks = mirChunk.getAllBlocksForRoomID(roomID);
                if(blocks.isEmpty())
                    continue;

                blocksForEachChunk.put(chunk, blocks);
            }

            Bukkit.getScheduler().runTask(NoXray.getInstance(), new Runnable() {
                @Override
                public void run() {
                    synchronized (syncLock) {
                        Iterator<Map.Entry<XZ, List<XYZ>>> chunks = blocksForEachChunk.entrySet().iterator();

                        while(chunks.hasNext()) {
                            Map.Entry<XZ, List<XYZ>> chunkBlocks = chunks.next();

                            List<XYZ> censoredBlocks = chunkBlocks.getValue();
                            Chunk chunk = _world.getChunkAt(chunkBlocks.getKey().x, chunkBlocks.getKey().z);

                            List<MapBlock> realBlocks = new ArrayList<MapBlock>();
                            for(XYZ coord : censoredBlocks) {
                                Block current = chunk.getBlock(coord.x, coord.y, coord.z);
                                realBlocks.add(new MapBlock(current.getTypeId(), current.getData(), coord.x, coord.y, coord.z));
                            }

                            new ChatPacketSender(player, ChatColor.GREEN + "A room was revealed!").send();
                            new BlockChangePacketSender(player, chunk.getX(), chunk.getZ(), realBlocks).send();
                        }
                    }
                }
            });

            _playerRooms.addVisibleRoomToPlayer(roomID, player);
        }
    }

    private class LocationRetreiver implements Runnable {

        public void run() {
            // Must be ran on primary thread
            if(!Bukkit.isPrimaryThread())
                return;

            _playerLocations.clear();

            // Don't need to worry about syncronizing because vector is thread safe
            // and PlayerLocation is immutable
            Vector<Player> players = new Vector<Player>(_world.getPlayers());

            for(Player p : players) {
                _playerLocations.add(new PlayerLocation(p, p.getLocation()));
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
                                // Only check Y blocks below feet to eye level
                                for(int iY = -1; iY < 2; iY++) {
                                    int roomID = _mirrorWorld.getRoomIDAtBlock(blockX + iX, blockY + iY, blockZ + iZ);
                                    if(roomID != 0)
                                        triggerPlayerHasSeenRoom(pl.getPlayer(), roomID);
                                }
                            }
                        }
                    }

                    // Schedule another sync task to update the player locations and do this again
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), new LocationRetreiver(), PLAYER_LOCATION_CHECK_FREQUENCY_TICKS);
                }
            });
        }
    }

    public void execute(Runnable task) {
        _executor.execute(task);
    }

    public MirrorWorld getMirrorWorld() {
        return _mirrorWorld;
    }

    // Called from main thread
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerHit(final PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(_world))
            return;

        if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;


        //todo
    }

    // Called from main thread
    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockBreak(final BlockBreakEvent event) {
        if(!event.getBlock().getWorld().equals(_world))
            return;

        // Don't try to autoprotect if the player has autoprotect off
        PlayerMetadataEntry metadata = NoXray.getInstance().getPlayerMetadata(event.getPlayer().getName());
        if(!metadata.isAutoProtectOn())
            return;

        Location loc = event.getPlayer().getEyeLocation();
        final int playerX = loc.getBlockX();
        final int playerY = loc.getBlockY();
        final int playerZ = loc.getBlockZ();
        final String playerName = event.getPlayer().getName();
        final int blockX = event.getBlock().getX();
        final int blockY = event.getBlock().getY();
        final int blockZ = event.getBlock().getZ();
        final Player p = event.getPlayer();

        execute(new Runnable() {
            @Override
            public void run() {
                // Don't try to autoprotect unless the player is actually in the protected area
                if(_mirrorWorld.getRoomIDAtBlock(playerX, playerY, playerZ) != 0) {

                    int[] rooms = _mirrorWorld.getRoomIDAtBlockAndAdjacent(blockX, blockY, blockZ);

                    // If the hit block is already protected, return - no need to protect
                    if(rooms[0] != 0)
                        return;

                    // We need to check all adjacent blocks incase we encounter two different rooms
                    int foundID = 0;
                    for(int i = 1; i < rooms.length; i++) {
                        int ID = rooms[i];

                        if(ID != 0) {
                            if(ID != foundID && foundID != 0) {
                                new ChatPacketSender(p, ChatColor.RED + "Multiple room IDs were found; block could not be autoprotected").send();
                                return;
                            }
                            else foundID = ID;
                        }
                    }

                    if(foundID == 0) {
                        new ChatPacketSender(p, ChatColor.RED + "The block was connected to an unprotected block");
                        return;
                    }

                    _mirrorWorld.setRoomIDAtBlock(blockX, blockY, blockZ, foundID);
                    new ParticlePacketSender(p, ParticlePacketSender.ParticleEffect.CRIT, blockX, blockY, blockZ).send(6);
                }
            }
        });
    }

    // Called from main thread
    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkLoad(final ChunkLoadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        final int x = event.getChunk().getX();
        final int z = event.getChunk().getZ();

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _mirrorWorld.loadMirrorChunk(x, z);
            }
        });
    }

    // Called from main thread
    @EventHandler (priority = EventPriority.MONITOR)
    private void onChunkUnload(final ChunkUnloadEvent event) {
        if(!event.getWorld().equals(_world))
            return;

        final int x = event.getChunk().getX();
        final int z = event.getChunk().getZ();

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _mirrorWorld.unloadMirrorChunk(x, z);
            }
        });
    }
}
