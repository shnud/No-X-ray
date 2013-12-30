package com.shnud.noxray.Structures;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public final class DynamicByteArray {

    /*
     * At the moment we only have enough room in the static buffer to use this
     * class for nibbled chunk data (65536 / 2 at the maximum). We could allocate
     * more but it would be a waste, and I can't figure out a way of only ever
     * allocating as much as necessary but at the same time using a static buffer.
     *
     * We could allocate the buffer dynamically for each instance but it would
     * be slower. If all the instances share the same buffer then we reduce the
     * amount of time spent allocating memory by a huge amount.
     */
    private static final byte[] _buffer = new byte[MagicValues.BLOCKS_IN_CHUNK / 2];
    private static final int MAX_SECONDS_UNTIL_RECOMPRESSION = 20;
    private static final int MINIMUM_MILLISECONDS_BETWEEN_SCHEDULING_TASKS = (MAX_SECONDS_UNTIL_RECOMPRESSION / 2) * 1000;
    private long _timeTimerLastReset;
    private boolean _isCompressed;
    private int _originalByteArrayLength = -1;
    private BukkitTask _compressionTask;
    private Runnable _compressionTaskRunner = new CompressionTaskRunner();
    private byte[] _byteArray;

    public static DynamicByteArray constructFromUncompressedByteArray(byte[] uncompressed) {
        return new DynamicByteArray(uncompressed, false);
    }

    public static DynamicByteArray constructFromCompressedByteArray(byte[] compressed) throws DataFormatException {
        return new DynamicByteArray(compressed, true);
    }

    private DynamicByteArray(byte[] array, boolean compressed) {
        _byteArray = array;
        _isCompressed = compressed;

        if(!_isCompressed) {
            _originalByteArrayLength = array.length;
            compress();
        }
    }

    /**
     * Get the byte array used to back this chunk data array
     *
     * @return the actual byte array; be careful
     */
    private byte[] getUncompressedByteArray() {
        if(_isCompressed)
            uncompress();

        return _byteArray;
    }

    /**
     * Get the compressed version of the byte array used to back this chunk data array.
     *
     * @return the actual byte array; be careful
     */
    public byte[] getCompressedByteArray() {
        if(!_isCompressed)
            compress();

        return _byteArray;
    }

    public byte getValueAtIndex(int index) {
        if(_isCompressed)
            uncompress();

        return _byteArray[index];
    }

    public void setValueAtIndex(int index, byte value) {
        if(_isCompressed)
            uncompress();

        _byteArray[index] = value;
    }

    private void resetCompressionTimer() {
        /*
         * We allow a quiet period so that Bukkit doesn't get bombarded with constant
         * request to cancel and reschedule tasks, as block requests will be made very
         * often.
         */

        if(System.currentTimeMillis() - _timeTimerLastReset < MINIMUM_MILLISECONDS_BETWEEN_SCHEDULING_TASKS)
            return;

        if(_compressionTask != null)
            _compressionTask.cancel();

        _compressionTask = Bukkit.getScheduler().runTaskLater(
                NoXray.getInstance(),
                _compressionTaskRunner,
                MagicValues.MINECRAFT_TICKS_PER_SECOND * MAX_SECONDS_UNTIL_RECOMPRESSION
        );

        _timeTimerLastReset = System.currentTimeMillis();
    }

    private void compress() {
        if(_isCompressed)
            return;

        _originalByteArrayLength = _byteArray.length;
        _byteArray = compressAndReturnResult(_byteArray);
        _isCompressed = true;
    }

    private void uncompress() {
        if(!_isCompressed)
            return;

        try {

            /*
             * If we already know the length of the original
             * byte array we can use a quicker method where
             * the uncompressed byte array does not need to be
             * written to a intermediary buffer.
             */

            if(_originalByteArrayLength > 0) {
                Inflater _inf = new Inflater();
                _inf.setInput(_byteArray);
                byte[] uncompressedArray = new byte[_originalByteArrayLength];
                _inf.inflate(uncompressedArray);
                _byteArray = uncompressedArray;
                _isCompressed = false;
                _inf.end();
            }
            else
                _byteArray = uncompressAndReturnResult(_byteArray);

            resetCompressionTimer();

        } catch (DataFormatException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Chunk data array was unable to be decompressed, room hiding data may have been lost");
        }
    }

    public static byte[] compressAndReturnResult(byte[] input) {
        Deflater _def = new Deflater();
        _def.setInput(input);
        int amountBytesCompressed = _def.deflate(_buffer);
        byte[] output = new byte[amountBytesCompressed];
        System.arraycopy(_buffer, 0, output, 0, amountBytesCompressed);
        _def.end();
        return output;
    }

    public static byte[] uncompressAndReturnResult(byte[] input) throws DataFormatException {
        Inflater _inf = new Inflater();
        _inf.setInput(input);
        int sizeOfUncompressed = _inf.inflate(_buffer);
        byte[] output = new byte[sizeOfUncompressed];
        System.arraycopy(_buffer, 0, output, 0, sizeOfUncompressed);
        _inf.end();
        return output;
    }

    private class CompressionTaskRunner implements Runnable {
        @Override
        public void run() {
            if(Bukkit.isPrimaryThread())
                compress();
        }
    }
}
