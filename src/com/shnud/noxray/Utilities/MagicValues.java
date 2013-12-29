package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 26/12/2013.
 */
public class MagicValues {

    public static final int MINECRAFT_TICKS_PER_SECOND = 20;
    public static final int CHUNKS_IN_REGION = 32 * 32;
    public static final int BLOCKS_IN_CHUNK = 16 * 16 * 16 * 16;
    public static final int BITSHIFTS_RIGHT_BLOCK_TO_CHUNK = 4;
    public static final int BITSHIFTS_RIGHT_CHUNK_TO_REGION = 5;
    public static final int BITSHIFTS_RIGHT_BLOCK_TO_REGION = BITSHIFTS_RIGHT_CHUNK_TO_REGION + BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;
}
