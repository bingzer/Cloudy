package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.RemoteFile;

/**
 * Sync manager
 */
public interface ISyncManager {

    int SYNC_INCREMENT = 0;
    int SYNC_DUMP_TO_REMOTE = 1;
    int SYNC_DUMP_TO_LOCAL = 2;

    //////////////////////////////////////////////////////////////////////////////////////////////

    IEnvironment getLocalEnvironment();

    IEnvironment getRemoteEnvironment();

    IEntityHistory createEntityHistory(IEnvironment environment);

    /**
     * Returns the Root
     */
    RemoteFile getRoot();

    RemoteFile getRemoteDbFile();

    /**
     * Sync database
     * @param syncType the type of sync {@link #SYNC_INCREMENT}, {@link #SYNC_DUMP_TO_REMOTE}, {@link #SYNC_DUMP_TO_LOCAL}
     */
    void syncDatabase(int syncType) throws SyncException;

}
