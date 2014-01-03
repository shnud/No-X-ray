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

    public static DynamicCoordinates initWithChunkCoordinates(int chunkX, int chunkY, int chunkZ) {
        return new DynamicCoordinates(
                chunkX * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK,
                chunkY,
                chunkZ * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK,
                PrecisionLevel.CHUNK
        );
    }

    public static DynamicCoordinates initWithRegionCoordinates(int regionX, int regionY, int regionZ) {
        return new DynamicCoordinates(
                regionX * MagicValues.HORIZONTAL_CHUNKS_IN_REGION * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK,
                regionY,
                regionZ * MagicValues.HORIZONTAL_CHUNKS_IN_REGION * MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK,
                PrecisionLevel.REGION
        );
    }

    public static DynamicCoordinates initWithBlockCoordinates(int blockX, int blockY, int blockZ) {
        return new DynamicCoordinates(blockX, blockY, blockZ, PrecisionLevel.BLOCK);
    }

    public int blockX() {return _blockX;}
    public int blockY() {return _blockY;}
    public int blockZ() {return _blockZ;}
    public int chunkX() {return _blockX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int chunkY() {return _blockY;}
    public int chunkZ() {return _blockZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int regionX() {return _blockX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int regionY() {return _blockY;}
    public int regionZ() {return _blockZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int chunkRelativeBlockX() { return (blockX() < 0 ? -blockX() : blockX()) % MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK; }
    public int chunkRelativeBlockY() { return blockY(); }
    public int chunkRelativeBlockZ() { return (blockZ() < 0 ? -blockZ() : blockZ()) % MagicValues.HORIZONTAL_BLOCKS_IN_CHUNK; }
    public int regionRelativeChunkX() { return (chunkX() < 0 ? -chunkX() : chunkX()) % MagicValues.HORIZONTAL_CHUNKS_IN_REGION; }
    public int regionRelativeChunkY() { return chunkY(); }
    public int regionRelativeChunkZ() { return (chunkZ() < 0 ? -chunkZ() : chunkZ()) % MagicValues.HORIZONTAL_CHUNKS_IN_REGION; }

    public PrecisionLevel getPrecisionLevel() {
        return _level;
    }
}
