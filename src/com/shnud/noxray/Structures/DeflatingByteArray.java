package com.shnud.noxray.Structures;

import com.shnud.noxray.NoXray;

import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.zip.*;

/**
 * A byte array that dynamically compresses itself when not in use in order to save space
 */
@ThreadSafe
public final class DeflatingByteArray extends ByteArray {

    private static final ThreadLocal<ByteArrayOutputStream> _buffer = new ThreadLocal();
    private static final ThreadLocal<Timer> _timer = new ThreadLocal();
    private static final int SECONDS_NOT_ACCESSED_UNTIL_RECOMPRESSION = 20;
    private TimerTask _compressionTask;
    private long _lastAccess = 0;
    private final int _uncompressedLength;
    private boolean _isCompressed;
    // The lock used to ensure that access is not attempted during inactive compression on the timer thread
    private final Object _lock = new Object();

    public static DeflatingByteArray newWithCompressedArray(final byte[] array) {
        return new DeflatingByteArray(array, true);
    }

    public static DeflatingByteArray newWithUncompressedArray(final byte[] array) {
        return new DeflatingByteArray(array, false);
    }

    /**
     * Creates a new dynamically compressed byte array from an already existing byte array
     * @param array  the byte array to wrap
     * @param inputIsCompressed whether the byte array is already deflated
     */
    public DeflatingByteArray(final byte[] array, final boolean inputIsCompressed) {
        super(array);

        if (!inputIsCompressed) {
            _uncompressedLength = array.length;
            resetCompressionTimer();
        }
        else
            _uncompressedLength = uncompressAndReturnResult(array).length;

        _isCompressed = inputIsCompressed;
    }

    /**
     * Creates a new dynamically compressed byte array from an already existing byte array
     * @param array the byte array to wrap (assumes uncompressed)
     */
    public DeflatingByteArray(final byte[] array) {
        this(array, false);
    }

    /**
     * Creates a new dynamically compressed byte array of the given size
     */
    public DeflatingByteArray(int size) {
        this(new byte[size]);
    }

    /**
     * Get the uncompressed byte array used to back this chunk data array
     * @return a copy of the uncompressed byte array, or the compressed array if inflation wasn't possible
     */
    public byte[] getPrimitiveByteArray() {
        synchronized (_lock) {
            if(_isCompressed)
                return uncompressAndReturnResult(_byteArray);

            return Arrays.copyOf(_byteArray, _byteArray.length);
        }
    }

    /**
     * Get the compressed version of the byte array
     */
    public byte[] getCompressedPrimitiveByteArray() {
        synchronized (_lock) {
            if(!_isCompressed)
                return compressAndReturnResult(_byteArray);
            else
                return Arrays.copyOf(_byteArray, _byteArray.length);
        }
    }

    public byte getValueAtIndex(int index) {
        synchronized (_lock) {
            if(_isCompressed)
                tryToUncompress();

            didAccess();
            return super.getValueAtIndex(index);
        }
    }

    public void setValueAtIndex(int index, byte value) {
        synchronized (_lock) {
            if(_isCompressed)
                tryToUncompress();

            didAccess();
            super.setValueAtIndex(index, value);
        }
    }

    public int size() {
        return _uncompressedLength;
    }

    public void clear() {
        synchronized (_lock) {
            if(_isCompressed)
                tryToUncompress();

            didAccess();
            super.clear();
        }
    }

    public boolean isCompressed() {
        return _isCompressed;
    }

    private void resetCompressionTimer() {
        _compressionTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (_lock) {
                    if(!_isCompressed && inactiveLongEnoughToCompress())
                        compress();
                }
            }
        };

        getTimer().scheduleAtFixedRate(
                _compressionTask,
                SECONDS_NOT_ACCESSED_UNTIL_RECOMPRESSION,
                SECONDS_NOT_ACCESSED_UNTIL_RECOMPRESSION
        );
    }

    private boolean inactiveLongEnoughToCompress() {
        return System.currentTimeMillis() - _lastAccess > SECONDS_NOT_ACCESSED_UNTIL_RECOMPRESSION;
    }

    private void compress() {
        synchronized (_lock) {
            if(_isCompressed)
                return;

            _byteArray = compressAndReturnResult(_byteArray);
            _isCompressed = true;
            _compressionTask.cancel();
        }
    }

    private void tryToUncompress() {
        try {
            uncompress();

        } catch (DataFormatException e) {
            NoXray.getInstance().getLogger().log(

                    Level.SEVERE,
                    "Chunk data array was unable to be decompressed, " +
                            "room hiding data may have been lost"

            );
        }
    }

    private void uncompress() throws DataFormatException {
        synchronized (_lock) {
            if(!_isCompressed)
                return;

            // If we already know the length of the original
            // byte array we can use a quicker method where
            // the uncompressed byte array does not need to be
            // written to a intermediary buffer.
            if(_uncompressedLength > 0) {
                Inflater _inf = new Inflater();
                _inf.setInput(_byteArray);
                byte[] uncompressedArray = new byte[_uncompressedLength];
                _inf.inflate(uncompressedArray);
                _byteArray = uncompressedArray;
                _inf.end();
            }
            else
                _byteArray = uncompressAndReturnResult(_byteArray);

            _isCompressed = false;
            resetCompressionTimer();
        }
    }

    private byte[] compressAndReturnResult(byte[] input) {
        ByteArrayOutputStream buffer = getBuffer();
        buffer.reset();
        DeflaterOutputStream stream = new DeflaterOutputStream(buffer);

        try {
            stream.write(input);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }

    private byte[] uncompressAndReturnResult(byte[] input) {
        ByteArrayOutputStream buffer = getBuffer();
        buffer.reset();
        InflaterOutputStream stream = new InflaterOutputStream(buffer);

        try {
            stream.write(input);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }

    private ByteArrayOutputStream getBuffer() {
        if(_buffer.get() == null)
            _buffer.set(new ByteArrayOutputStream(4096));

        return _buffer.get();
    }

    private static Timer getTimer() {
        if(_timer.get() == null)
            _timer.set(new Timer());

        return _timer.get();
    }

    private void didAccess() {
        _lastAccess = System.currentTimeMillis();
    }

    protected int getIntervalBetweenCompression() {
        return SECONDS_NOT_ACCESSED_UNTIL_RECOMPRESSION;
    }

    protected void forceCompress() {
        compress();
    }

    protected void forceUncompress() {
        tryToUncompress();
    }
}
