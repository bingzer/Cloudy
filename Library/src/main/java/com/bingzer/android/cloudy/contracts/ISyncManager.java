package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.driven.RemoteFile;

public interface ISyncManager {

    /**
     * Returns the Root
     */
    RemoteFile getRoot();

    /**
     * Sync database
     * @param dbRemoteFile dbRemote file
     */
    void syncDatabase(RemoteFile dbRemoteFile);

}
