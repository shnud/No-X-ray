package com.shnud.noxray.Utilities;

/**
 * Created by Andrew on 30/12/2013.
 */
public class DynamicCoordinates {

    private int _globalX, _globalY, _globalZ;
    private PrecisionLevel _level;

    private enum PrecisionLevel {
        BLOCK, CHUNK, REGION;
    }

    private DynamicCoordinates(int x, int y, int z, PrecisionLevel level) {
        _globalX = x;
        _globalY = y;
        _globalZ = z;
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

    public int globalX() {return _globalX;}
    public int globalY() {return _globalY;}
    public int globalZ() {return _globalZ;}
    public int chunkX() {return _globalX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int chunkY() {return _globalY >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int chunkZ() {return _globalZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_CHUNK;}
    public int regionX() {return _globalX >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int regionY() {return _globalY >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
    public int regionZ() {return _globalZ >> MagicValues.BITSHIFTS_RIGHT_BLOCK_TO_REGION;}
}
