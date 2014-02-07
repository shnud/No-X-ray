package com.shnud.noxray.RoomHiding;

import com.shnud.noxray.Packets.PacketHelpers.MapChunkDataWrapper;
import com.shnud.noxray.Structures.ByteArraySection;
import com.shnud.noxray.Structures.ByteWrappers.BooleanArray;
import com.shnud.noxray.Structures.ByteWrappers.NibbleArray;
import org.bukkit.Material;

import java.util.HashSet;

/**
 * Created by Andrew on 08/01/2014.
 */
public class ChunkCensor {

    /**
     * Takes a MapChunkDataWrapper and censors any blocks that are not contained in seen rooms for the specified mirror chunk. This function
     * works on the actual MapChunkDataWrapper and does not return a value
     *
     * @param chunk The MapChunkDataWrapper to censor
     * @param mirror The mirror chunk to take the hidden data from
     * @param seenRooms The list of rooms that the chunk censor should not hide
     * @param censorBlock The block that should be used to replace hidden blocks
     *
     * @return The number of blocks censored in the chunk
     */
    public static int censorChunk(MapChunkDataWrapper chunk, MirrorChunk mirror, HashSet<Integer> seenRooms,
                                Material censorBlock) {
        if(chunk == null) throw new IllegalArgumentException("Chunk cannot be null");
        if(mirror == null) throw new IllegalArgumentException("Mirror chunk cannot be null");

        if(chunk.isEmpty() || mirror.isEmpty())
            return 0;

        // This will be used to store all of the blocks that are non solid for each layer as we traverse
        // down the chunk. Any protected block which has an unprotected non solid block above it must
        // not be censored because it is possible to create pitfalls this way
        BooleanArray nonSolidAbove = new BooleanArray(256);
        int blockChangeCount = 0;

        // Add the mechanism to hide columns which are not covered by solid blocks
        for(int sectionI = 15; sectionI >= 0; sectionI--) {
            if(mirror.isSectionEmpty(sectionI))
                continue;

            ByteArraySection blockIDs = chunk.getPrimaryIDSection(sectionI);
            // If there are no block IDs for this section then there can't be any other
            // information to go with it so skip this section
            if(blockIDs == null)
                continue;

            NibbleArray metadata = chunk.getMetadataSection(sectionI);
            NibbleArray blockLight = chunk.getBlockLightSection(sectionI);
            NibbleArray additional = chunk.getAdditionalIDSection(sectionI);

            // Because we're accessing the mirror chunk from one big array rather than
            // the sections that are stored in the map chunk we have to store the offset
            // in the mirror chunk's array to start at
            int mirrorChunkOffset = sectionI * MapChunkDataWrapper.BLOCKS_PER_SECTION;

            for(int i = MapChunkDataWrapper.BLOCKS_PER_SECTION - 1; i >= 0; i--) {
                int indexXZ = i % 256;
                int roomID = mirror.getRoomIDAtIndex(i + mirrorChunkOffset);

                // If the player has not seen the room which this block is a part of, replace it with
                // whichever block is most suitable for the world type we're in
                if(roomID != 0 && (seenRooms == null || !seenRooms.contains(roomID))) {

                    // If the block above wasn't solid and it was unprotected, don't protect this block
                    if(!nonSolidAbove.getValueAtIndex(indexXZ)) {
                        blockIDs.setValueAtIndex(i, (byte) censorBlock.getId());
                        metadata.setValueAtIndex(i, 0);
                        blockLight.setValueAtIndex(i, 0);
                        if(additional != null)
                            additional.setValueAtIndex(i, 0);

                        blockChangeCount++;
                    }
                }

                Material blockType = Material.getMaterial(blockIDs.getValueAtIndex(i) & 0xFF);

                // Don't update the solid block above status if we're in a room
                if(roomID != 0)
                    continue;

                if(!blockType.isSolid())
                    nonSolidAbove.setValueAtIndex(true, indexXZ);
                else
                    nonSolidAbove.setValueAtIndex(false, indexXZ);
                // Set the block air status after processing this block as it will always be used
                // for the block below
            }
        }

        return blockChangeCount;
    }
}
