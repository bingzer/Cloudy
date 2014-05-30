package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.driven.RemoteFile;

public interface ISyncManager {

    long getClientId();

    /**
     * Returns the Root
     */
    RemoteFile getRoot();

    /**
     * Sync database
     * @param local the local database
     * @param dbRemoteFile dbRemote file
     */
    void syncDatabase(IEnvironment local, RemoteFile dbRemoteFile);

}
