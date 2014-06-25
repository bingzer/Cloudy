package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.driven.RemoteFile;

/**
 * Sync manager
 */
public interface ISyncManager {

    /**
     * Returns the Root
     */
    RemoteFile getRoot();

    /**
     * Sync database
     * @param dbRemoteFile dbRemote file
     */
    void syncDatabase(RemoteFile dbRemoteFile) throws SyncException;

}
