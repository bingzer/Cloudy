package com.bingzer.android.cloudy.contracts;

import android.content.Context;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;

/**
 * Sync manager
 */
public interface ISyncManager {

    int SYNC_INCREMENT = 0;
    int SYNC_DUMP_TO_REMOTE = 1;
    int SYNC_DUMP_TO_LOCAL = 2;
    int SYNC_FILES = 3;

    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The context
     */
    Context getContext();

    /**
     * Returns the local environment
     */
    IEnvironment getLocalEnvironment();

    /**
     * Creates an IEntityHistory
     */
    IDeleteHistory createDeleteHistory(IEnvironment environment);

    /**
     * Returns the Root
     */
    RemoteFile getRoot();

    /**
     * Returns the remote database file
     */
    RemoteFile getRemoteDbFile();

    /**
     * Returns the local config
     */
    ILocalConfiguration getConfig(String name);

    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sync database
     * @param syncType the type of sync {@link #SYNC_INCREMENT}, {@link #SYNC_DUMP_TO_REMOTE}, {@link #SYNC_DUMP_TO_LOCAL}
     */
    void syncDatabase(int syncType) throws SyncException;

    /**
     * Sync directory
     * @param dir the local directory
     * @param remoteDir the remote directory
     * @throws SyncException
     */
    void syncFiles(File dir, RemoteFile remoteDir) throws SyncException;

}
