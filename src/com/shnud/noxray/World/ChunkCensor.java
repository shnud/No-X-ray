package com.shnud.noxray.World;

import com.shnud.noxray.Structures.BooleanArray;
import com.shnud.noxray.Structures.ByteArraySection;
import com.shnud.noxray.Structures.NibbleArray;
import com.shnud.noxray.Structures.VariableBitArray;
import org.bukkit.Material;

import java.util.HashSet;

/**
 * Created by Andrew on 08/01/2014.
 */
public class ChunkCensor {

    public static void censorChunk(MapChunkData chunk, MirrorChunk mirror, HashSet<Integer> seenRooms, Material censorBlock) {
        if(chunk == null) throw new IllegalArgumentException("Chunk cannot be null");
        if(mirror == null) throw new IllegalArgumentException("Mirror chunk cannot be null");

        if(chunk.isEmpty() || mirror.isEmpty())
            return;

        // This will be used to store all of the blocks that are non solid for each layer as we traverse
        // down the chunk. Any protected block which has an unprotected non solid block above it must
        // not be censored because it is possible to create pitfalls this way
        BooleanArray nonSolidAbove = new BooleanArray(256);

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
            int mirrorChunkOffset = sectionI * MapChunkData.BLOCKS_PER_SECTION;

            for(int i = MapChunkData.BLOCKS_PER_SECTION - 1; i >= 0; i--) {
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
                    }
                }

                Material blockType = Material.getMaterial(blockIDs.getValueAtIndex(i) & 0xFF);

                if(!blockType.isSolid() && roomID == 0)
                    nonSolidAbove.setValueAtIndex(true, indexXZ);
                else
                    nonSolidAbove.setValueAtIndex(false, indexXZ);
                // Set the block air status after processing this block as it will always be used
                // for the block below

            }
        }
    }
}
