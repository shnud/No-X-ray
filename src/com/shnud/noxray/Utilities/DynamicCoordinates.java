package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 30/12/2013.
 */
public class DynamicCoordinates {

    private int _blockX, _blockY, _blockZ;
    private PrecisionLevel _level;

    public static enum PrecisionLevel {
        BLOCK, CHUNK, REGION;
    }

    private DynamicCoordinates(int x, int y, int z, PrecisionLevel level) {
        _blockX = x;
        _blockY = y;
        _blockZ = z;
        _level = level;
    }

    private static DynamicCoordinates initWithChunkCoordinates(int chunkX, int chunkY, int chunkZ) {
        return new DynamicCoordinates(
                chunkX * MagicValues.BLOCKS_IN_CHUNK,
                chunkY * MagicValues.BLOCKS_IN_CHUNK,
                chunkZ * MagicValues.BLOCKS_IN_CHUNK,
                PrecisionLevel.CHUNK
        );
    }

    private static DynamicCoordinates initWithRegionCoordinates(int regionX, int regionY, int regionZ) {
        return new DynamicCoordinates(
                regionX * MagicValues.CHUNKS_IN_REGION * MagicValues.BLOCKS_IN_CHUNK,
                regionY * MagicValues.CHUNKS_IN_REGION * MagicValues.BLOCKS_IN_CHUNK,
                regionZ * MagicValues.CHUNKS_IN_REGION * MagicValues.BLOCKS_IN_CHUNK,
                PrecisionLevel.REGION
        );
    }

    private static DynamicCoordinates initWithBlockCoordinates(int blockX, int blockY, int blockZ) {
        return new DynamicCoordinates(blockX, blockY, blockZ, PrecisionLevel.BLOCK);
    }

    public int blockX() {return _blockX;}
    public int blockY() {return _blockY;}
    public int blockZ() {return _blockZ;}
    public int chunkX() {return _blockX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int chunkY() {return _blockY >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int chunkZ() {return _blockZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int regionX() {return _blockX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int regionY() {return _blockY >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int regionZ() {return _blockZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public PrecisionLevel getPrecisionLevel() {
        return _level;
    }
}
