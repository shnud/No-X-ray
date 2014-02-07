package com.shnud.noxray.RoomHiding;

import com.shnud.noxray.Concurrency.BasicExecutor;
import com.shnud.noxray.NoXray;
import com.shnud.noxray.Packets.IPacketEventWrapperListener;
import com.shnud.noxray.Packets.PacketEventListener;
import com.shnud.noxray.Packets.PacketEvents.BlockChangePacketEvent;
import com.shnud.noxray.Packets.PacketEvents.MapChunkPacketEvent;
import com.shnud.noxray.Packets.PacketEvents.NoXrayPacketEvent;
import com.shnud.noxray.Packets.PacketHelpers.AbstractBlockChangePacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.AbstractMapChunkPacketHelper;
import com.shnud.noxray.Packets.PacketHelpers.MapBlock;
import com.shnud.noxray.Packets.PacketHelpers.MapChunkDataWrapper;
import com.shnud.noxray.Packets.PacketSenders.BlockChangePacketSender;
import com.shnud.noxray.Packets.PacketSenders.ChatPacketSender;
import com.shnud.noxray.Packets.PacketSenders.ParticlePacketSender;
import com.shnud.noxray.Settings.PlayerMetadataEntry;
import com.shnud.noxray.Utilities.DynamicCoordinates;
import com.shnud.noxray.Utilities.MagicValues;
import com.shnud.noxray.Utilities.XYZ;
import com.shnud.noxray.Utilities.XZ;
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
public class RoomHider implements Listener, IPacketEventWrapperListener {

    // The frequency at which we should update the list of player locations
    private static final int PLAYER_LOCATION_CHECK_FREQUENCY_TICKS = 20 * 3;

    // The associated world this room hider is hiding rooms for
    private final World _world;

    // The mirror world which stores the mirror chunks with hidden room data
    private final MirrorWorld _mirrorWorld;

    // The list of rooms for this world, containing the known chunks for each room
    private final RoomList _rooms;

    // The executor for this room hider - each runs on its own thread
    private final BasicExecutor _executor;

    // An array list containing the latest player locations retreived from the main thread
    private final ArrayList<PlayerLocation> _playerLocations = new ArrayList<PlayerLocation>();

    // An object that contains all the rooms each player has seen
    private final PlayerSeenRooms _playerRooms = new PlayerSeenRooms();

    // The material to use in this world to fill in blocks which are hidden
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
        _executor = new BasicExecutor("Room hiding thread for world: " + world.getName());
        _executor.start();

        // Listen for packets so we can filter block changes and map chunks
        PacketEventListener.get().addListener(this);

        // Add this to the bukkit listeners so we can load and unload chunks
        NoXray.getInstance().getServer().getPluginManager().registerEvents(this, NoXray.getInstance());

        // Start the player location retreiver task that will continue until plugin is disabled
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), new LocationRetriever(), PLAYER_LOCATION_CHECK_FREQUENCY_TICKS);
    }

    /**
     * Get the minecraft world this room hider is associated with
     * @return the world
     */
    public World getWorld() {
        return _world;
    }

    /**
     * Get the mirror world where the mirror chunks are stored
     * @return the mirror world
     */
    public MirrorWorld getMirrorWorld() {
        return _mirrorWorld;
    }

    /**
     * Saves all of the room data
     */
    public void disable() {
        _executor.execute(new Runnable() {
            @Override
            public void run() {
                _rooms.saveRooms();
                _mirrorWorld.saveAllRegions();
            }
        });

        _executor.cancel();
    }

    /**
     * Attempt to hide a room at the location of a player (called from main thread)
     * This relays messages back to the player depending on the result of the attempt (we should probably deal with that elsewhere somehow)
     *
     * @param player the player to attempt hiding for
     */
    public void hideAtPlayerLocation(final Player player) {
        if(player.getWorld() != _world)
            return;

        // Should probably make sure we don't add any new hidden blocks while blocks
        // are being retreived to send to players who have just seen rooms
        // Not the biggest of issues, though

        player.sendMessage(ChatColor.YELLOW + "Searching...");
        final Object syncLock = new Object();
        final List<XYZ> blocks = new ArrayList<XYZ>();

        synchronized (syncLock) {
            Location loc = player.getEyeLocation();
            RoomSearcher searcher = new RoomSearcher(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), _world);

            try {
                blocks.addAll(searcher.searchAndReturnRoomBlocks());

                if(blocks.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No blocks were found");
                    return;
                }
            } catch (RoomSearcher.ChunkNotLoadedException e) {
                player.sendMessage(ChatColor.RED + "Search area was too large; is the area properly enclosed?");
                return;
            } catch (RoomSearcher.MaxBlocksReachedException e) {
                player.sendMessage(ChatColor.RED + "Search area was too large; is the area properly enclosed?");
                return;
            }
        }

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (syncLock) {}

                boolean multipleIDsFound = false;

                // The new room ID we will use for the blocks that are found
                int newRoomID = 0;

                // Here we search the blocks for an already existing ID so we can merge if possible
                for(XYZ block : blocks) {
                    int blockID = _mirrorWorld.getRoomIDAtBlock(block.x, block.y, block.z);

                    if(blockID != 0) {
                        if(newRoomID == 0) newRoomID = blockID;
                        else if(newRoomID != blockID) multipleIDsFound = true;
                    }
                }

                if(multipleIDsFound) {
                    new ChatPacketSender(player, ChatColor.YELLOW +
                            "More than one different room was found while searching").send();
                    new ChatPacketSender(player, ChatColor.YELLOW +
                            "Please unhide one of these areas first if you wish to use a single ID").send();

                    return;
                }

                if(newRoomID == 0)
                    newRoomID = _rooms.getUnusedRoomID();

                int successBlocks = 0;
                int particleInterval = 10;
                int maxParticles = 50;
                int particlesSent = 0;

                for(XYZ block : blocks) {
                    if(_mirrorWorld.setRoomIDAtBlock(block.x, block.y, block.z, newRoomID)) {

                        successBlocks++;
                        _rooms.addKnownChunkToRoom(block.x >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, block.z >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, newRoomID);

                        if(successBlocks % particleInterval == 0 && particlesSent < maxParticles) {
                            new ParticlePacketSender(player, ParticlePacketSender.ParticleEffect.CRIT, block.x, block.y, block.z).send();

                            particlesSent++;
                        }
                    }
                }

                if(successBlocks == 0)
                    new ChatPacketSender(player, ChatColor.RED + "Hiding was unsuccessful").send();
                else {
                    new ChatPacketSender(player, ChatColor.GREEN + "Hiding successful! " + successBlocks + " blocks were hidden").send();
                    new ChatPacketSender(player, ChatColor.GRAY + "Room ID: " + newRoomID).send();

                    _playerRooms.addVisibleRoomToPlayer(newRoomID, player);
                }
            }
        });

    }

    /**
     * Attempt to unhide a room at the location of a player (called from main thread)
     * This relays messages back to the player depending on the result of the attempt (we should probably deal with that elsewhere somehow)
     *
     * @param player the player to attempt unhiding for
     */
    public void unHideAtPlayerLocation(final Player player) {
        if(player.getWorld() != _world)
            return;

        final Location loc = player.getEyeLocation();
        final int playerX = loc.getBlockX();
        final int playerY = loc.getBlockY();
        final int playerZ = loc.getBlockZ();

        execute(new Runnable() {
            @Override
            public void run() {
                int roomID = _mirrorWorld.getRoomIDAtBlock(playerX, playerY, playerZ);
                if(roomID == 0) {
                    new ChatPacketSender(player, ChatColor.RED + "No hidden blocks found at eye level");
                    return;
                }

                HashSet<XZ> chunks = _rooms.getKnownChunksForRoom(roomID);

                for (XZ chunk : chunks) {
                    if(!_mirrorWorld.isMirrorChunkLoaded(chunk.x, chunk.z))
                        continue;

                    MirrorChunk mirror = _mirrorWorld.getMirrorChunk(chunk.x, chunk.z);
                    mirror.removeRoom(roomID);
                }

                if(chunks.size() > 0) {
                    new ChatPacketSender(player, ChatColor.GREEN + "Room (ID: " + roomID + ") was unhidden from " + chunks.size() + " chunks").send();
                }
                else {
                    // If we couldn't find the chunks that the room was in (shouldn't really happen), just remove them from the chunk that the player is in

                    DynamicCoordinates coords = DynamicCoordinates.initWithBlockCoordinates(playerX, playerY, playerZ);
                    MirrorChunk chunk = _mirrorWorld.getMirrorChunk(coords);

                    chunk.removeRoom(roomID);

                    new ChatPacketSender(player, ChatColor.RED + "It was not possible to find all the chunks room " + roomID + " was contained within").send();
                    new ChatPacketSender(player, ChatColor.RED + "Fragments of the room may still be hidden").send();
                }

                _rooms.removeRoom(roomID);
            }
        });
    }

    /**
     * Handle a packet event generated by the PacketEventListener (called from protocolLib async thread)
     * @param event the packet event
     */
    @Override
    public void receivePacketEvent(NoXrayPacketEvent event) {
        // We don't care about the event if it's not regarding this world
        if(event.getReceiver().getWorld() != _world)
            return;

        if(event instanceof MapChunkPacketEvent)
            handleMapChunkPacketEvent((MapChunkPacketEvent) event);
        else if(event instanceof BlockChangePacketEvent)
            handleBlockChangePacketEvent((BlockChangePacketEvent) event);
    }

    /**
     * Handle a map chunk packet event (called from protocolLib async thread)
     * @param event the packet event
     */
    private void handleMapChunkPacketEvent(final MapChunkPacketEvent event) {
        // Before we hand over to our room hiding thread, make sure we increment the
        // processing delay so that it doesn't send as soon as it returns
        event.incrementAsyncProcessingCount();

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (event.getProcessingLock()) {
                    AbstractMapChunkPacketHelper helper = event.getMapChunkPacketHelper();
                    boolean dataChanged = false;

                    for (int chunkI = 0; chunkI < helper.getAmountOfChunks(); chunkI++) {
                        MapChunkDataWrapper chunk = helper.getMutableChunk(chunkI);
                        DynamicCoordinates coords = DynamicCoordinates.initWithChunkCoordinates(chunk.getX(), 0,
                                chunk.getZ());

                        // If the mirror chunk isn't loaded then it means there was no hidden data for it, so
                        // there's no need to filter it
                        if (!_mirrorWorld.isMirrorChunkLoaded(coords))
                            continue;

                        MirrorChunk mirror = _mirrorWorld.getMirrorChunk(coords);
                        HashSet<Integer> seenRooms = _playerRooms.getVisibleRoomsForPlayer(event.getReceiver());
                        if(ChunkCensor.censorChunk(chunk, mirror, seenRooms, _censorBlock) > 0)
                            dataChanged = true;
                    }

                    // Ensure that we recompress the modified data as that's what gets sent to the client
                    if(dataChanged)
                        helper.packDataForSending();
                    
                    event.decrementAsyncProcessingCountAndSendIfZero();
                }
            }
        });
    }

    /**
     * Handle a block change packet event (called from protocolLib async thread)
     * @param event the packet event
     */
    private void handleBlockChangePacketEvent(final BlockChangePacketEvent event) {
        // Before we hand over to our room hiding thread, make sure we increment the
        // processing delay so that it doesn't send as soon as it returns
        event.incrementAsyncProcessingCount();

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (event.getProcessingLock()) {
                    Player player = event.getReceiver();
                    AbstractBlockChangePacketHelper helper = event.getBlockChangePacketHelper();

                    LinkedList<MapBlock> blockChanges = helper.getBlockChanges();
                    boolean blockChangesModified = false;
                    Iterator<MapBlock> blockChangeIt = blockChanges.iterator();

                    while (blockChangeIt.hasNext()) {
                        MapBlock current = blockChangeIt.next();

                        Room room = new Room(
                                _mirrorWorld.getRoomIDAtBlock(
                                        current.getX(),
                                        current.getY(),
                                        current.getZ()
                                )
                        );

                        if (room.isValidRoom() && !_playerRooms.isRoomVisibleForPlayer(room, player)) {
                            blockChangeIt.remove();
                            blockChangesModified = true;
                        }
                    }

                    // Only set the new list if any blocks have been changed so
                    // as to save re-adding the same blocks all over again
                    if (blockChangesModified) {
                        if (!blockChanges.isEmpty())
                            helper.setBlockChanges(blockChanges);
                        else
                            event.cancel();
                    }

                    event.decrementAsyncProcessingCountAndSendIfZero();
                }
            }
        });
    }

    /**
     * Reveal a room to a player
     * @param player the player to reveal the room to
     * @param roomID the roomID to reveal
     */
    private void revealRoomToPlayer(final Player player, final int roomID) {
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
                if(!_mirrorWorld.isMirrorChunkLoaded(chunk.x, chunk.z))
                    continue;

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

                            new BlockChangePacketSender(player, chunk.getX(), chunk.getZ(), realBlocks).send();
                        }
                    }
                }
            });

            new ChatPacketSender(player, ChatColor.GREEN + "A room was revealed!").send();
            _playerRooms.addVisibleRoomToPlayer(roomID, player);
        }
    }

    /**
     * Execute a runnable on the room hiding thread
     * @param task the task to be run
     */
    public void execute(Runnable task) {
        _executor.execute(task);
    }

    /**
     * Handle a block hit event (called from the main thread)
     * If the block the player hits is part of a hidden room it will be revealed to them
     * @param event the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerHit(final PlayerInteractEvent event) {
        if(!event.getPlayer().getWorld().equals(_world))
            return;

        if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        final int blockX = event.getClickedBlock().getX();
        final int blockY = event.getClickedBlock().getY();
        final int blockZ = event.getClickedBlock().getZ();
        final Player p = event.getPlayer();

        execute(new Runnable() {
            @Override
            public void run() {
                int id = _mirrorWorld.getRoomIDAtBlock(blockX, blockY, blockZ);
                if(id != 0)
                    revealRoomToPlayer(p, id);
            }
        });
    }

    /**
     * Handle a block break event (called from the main thread)
     * If autohide is turned on for the player that broke the block, hiding of the block will be attempted     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onBlockBreak(final BlockBreakEvent event) {
        if(!event.getBlock().getWorld().equals(_world))
            return;

        // Just incase the block break event hasn't been caused by a player
        if(event.getPlayer() == null)
            return;

        // Don't try to autohide if the player has autohide off
        PlayerMetadataEntry metadata = NoXray.getInstance().getPlayerMetadata(event.getPlayer().getName());
        if(!metadata.isAutohideOn())
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

        final Object lock = new Object();
        final boolean[] airBlocks;

        synchronized (lock) {
            airBlocks = new boolean[6];
            airBlocks[0] = _world.getBlockAt(blockX - 1, blockY, blockZ).getType() == Material.AIR;
            airBlocks[1] = _world.getBlockAt(blockX + 1, blockY, blockZ).getType() == Material.AIR;
            airBlocks[2] = _world.getBlockAt(blockX, blockY - 1, blockZ).getType() == Material.AIR;
            airBlocks[3] = _world.getBlockAt(blockX, blockY + 1, blockZ).getType() == Material.AIR;
            airBlocks[4] = _world.getBlockAt(blockX, blockY, blockZ - 1).getType() == Material.AIR;
            airBlocks[5] = _world.getBlockAt(blockX, blockY, blockZ + 1).getType() == Material.AIR;
        }

        execute(new Runnable() {
            @Override
            public void run() {
                // Don't try to autohide unless the player is actually in the hidden area
                if(_mirrorWorld.getRoomIDAtBlock(playerX, playerY, playerZ) == 0)
                    return;

                int[] rooms = _mirrorWorld.getRoomIDAtBlockAndAdjacent(blockX, blockY, blockZ);

                // If the hit block is already hidden, return - no need to hide
                if(rooms[0] != 0)
                    return;

                int foundID = 0;
                synchronized (lock) {
                    // We need to check all adjacent blocks incase we encounter two different rooms
                    for(int i = 1; i < rooms.length; i++) {
                        int ID = rooms[i];

                        if(ID == 0 && airBlocks[i - 1]) {
                            new ChatPacketSender(p, ChatColor.RED + "The block was connected to an unhidden air block").send();
                            return;
                        }
                        if(ID != 0) {
                            if(ID != foundID && foundID != 0) {
                                new ChatPacketSender(p, ChatColor.RED + "Multiple room IDs were found; block could not be autohidden").send();
                                return;
                            }
                            else foundID = ID;
                        }
                    }
                }

                _mirrorWorld.setRoomIDAtBlock(blockX, blockY, blockZ, foundID);
                _rooms.addKnownChunkToRoom(blockX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, blockZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK, foundID);
                new ParticlePacketSender(p, ParticlePacketSender.ParticleEffect.CRIT, blockX, blockY, blockZ).send(6);
            }

        });
    }

    /**
     * Handle a chunk load event (called from the main thread)     *
     * This will load the matching mirror chunk
     *
     * @param event the event
     */
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

    /**
     * Handle a chunk unload event (called from the main thread)     *
     * This will unload the matching mirror chunk
     *
     * @param event the event
     */
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

    /**
     * The runnable we use to collect player location data every so often from the main thread
     */
    private class LocationRetriever implements Runnable {

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

                        for(int iX = -1; iX < 2; iX++) {
                            for(int iZ = -1; iZ < 2; iZ++) {
                                // Only check Y blocks below feet to eye level
                                for(int iY = -1; iY < 2; iY++) {
                                    int roomID = _mirrorWorld.getRoomIDAtBlock(blockX + iX, blockY + iY, blockZ + iZ);
                                    if(roomID != 0)
                                        revealRoomToPlayer(pl.getPlayer(), roomID);
                                }
                            }
                        }
                    }

                    // Schedule another sync task to update the player locations and do this again
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NoXray.getInstance(), new LocationRetriever(), PLAYER_LOCATION_CHECK_FREQUENCY_TICKS);
                }
            });
        }
    }
}
