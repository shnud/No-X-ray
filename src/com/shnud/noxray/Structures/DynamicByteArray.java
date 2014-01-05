package com.shnud.noxray.Structures;

import com.shnud.noxray.NoXray;
import com.shnud.noxray.Utilities.MagicValues;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public final class DynamicByteArray extends ByteArray {

    /*
     * At the moment the size of the buffer is just the maximum amount of blocks in a chunk, as this is the only thing we using this class for.
     * If however we use this class for another, totally different purpose it will be necessary to rethink how to dynamicaaly change the size of
     * the buffer depending on the byte array that this wraps around.
     */
    private static final byte[] _buffer = new byte[MagicValues.BLOCKS_IN_CHUNK];
    private static final int MAX_SECONDS_UNTIL_RECOMPRESSION = 20;
    private static final int MINIMUM_MILLISECONDS_BETWEEN_SCHEDULING_TASKS = (MAX_SECONDS_UNTIL_RECOMPRESSION / 2) * 1000;
    private long _timeTimerLastReset;
    private boolean _isCompressed;
    private int _originalByteArrayLength = -1;
    private BukkitTask _compressionTask;
    private Runnable _compressionTaskRunner = new CompressionTaskRunner();

    /**
     * Creates a new dynamically compressed byte array from an already existing byte array
     * @param array the byte array to wrap (assumes uncompressed)
     */
    public DynamicByteArray(byte[] array) {
        this(array, false);
    }

    /**
     * Creates a new dynamically compressed byte array from an already existing byte array
     * @param array  the byte array to wrap
     * @param compressed whether the byte array is already deflated
     */
    public DynamicByteArray(byte[] array, boolean compressed) {
        super(array);
        _isCompressed = compressed;

        if(!_isCompressed) {
            _originalByteArrayLength = array.length;
            compress();
        }
    }

    /**
     * Creates a new dynamically compressed byte array of the given size
     */
    public DynamicByteArray(int size) {
        this(new byte[size]);
    }

    /**
     * Get the uncompressed byte array used to back this chunk data array
     * @return a copy of the uncompressed byte array, or the compressed array if inflation wasn't possible
     */
    public byte[] getPrimitiveByteArray() {
        if(_isCompressed) {
            try {
                return uncompressAndReturnResult(_byteArray);
            } catch (DataFormatException e) {
                NoXray.getInstance().getLogger().log(Level.SEVERE, "Unable to decompress byte array, returning compressed byte array instead");
                e.printStackTrace();
            }
        }

        return Arrays.copyOf(_byteArray, _byteArray.length);
    }

    /**
     * Get the compressed version of the byte array used to back this chunk data array
     * @return a copy of the compressed byte array
     */
    public byte[] getCompressedPrimitiveByteArray() {
        if(!_isCompressed)
            return compressAndReturnResult(_byteArray);
        else
            return Arrays.copyOf(_byteArray, _byteArray.length);
    }

    public byte getValueAtIndex(int index) {
        if(_isCompressed)
            uncompress();

        return super.getValueAtIndex(index);
    }

    public void setValueAtIndex(int index, byte value) {
        if(_isCompressed)
            uncompress();

        super.setValueAtIndex(index, value);
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
                _inf.end();
            }
            else
                _byteArray = uncompressAndReturnResult(_byteArray);

            _isCompressed = false;
            resetCompressionTimer();

        } catch (DataFormatException e) {
            NoXray.getInstance().getLogger().log(Level.SEVERE, "Chunk data array was unable to be decompressed, room hiding data may have been lost");
        }
    }

    private static byte[] compressAndReturnResult(byte[] input) {
        Deflater _def = new Deflater();
        _def.setInput(input);
        _def.finish();
        int amountBytesCompressed = _def.deflate(_buffer);
        byte[] output = new byte[amountBytesCompressed];
        System.arraycopy(_buffer, 0, output, 0, amountBytesCompressed);
        _def.end();
        return output;
    }

    private static byte[] uncompressAndReturnResult(byte[] input) throws DataFormatException {
        Inflater _inf = new Inflater();
        _inf.setInput(input, 0, input.length);
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

    public int size() {
        return _originalByteArrayLength;
    }

    public void clear() {
        if(_isCompressed)
            uncompress();

        super.clear();
    }
}
