package com.bingzer.android.cloudy;

/**
 * Default exception that's thrown
 * whenever an exception arises during syncing process.
 *
 * @see com.bingzer.android.cloudy.SyncException.NoChanges
 * @see com.bingzer.android.cloudy.SyncException.OtherIsSyncing
 */
public class SyncException extends RuntimeException {

    /**
     * An instance of SyncException with the message
     */
    public SyncException(String message){
        super(message);
    }

    /**
     * SyncException
     */
    public SyncException(Exception baseException){
        super(baseException);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Thrown when sync is not necessary. Everything is up-to-date
     */
    public static class NoChanges extends com.bingzer.android.cloudy.SyncException {
        public NoChanges() {
            super("No changes detected");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Thrown when other client is syncing first.
     * We must yield.
     */
    public static class OtherIsSyncing extends com.bingzer.android.cloudy.SyncException {
        public OtherIsSyncing() {
            super("Other client is syncing. Must yield.");
        }
    }
}
