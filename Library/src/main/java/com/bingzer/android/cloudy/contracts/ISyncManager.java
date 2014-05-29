package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.driven.RemoteFile;

public interface ISyncManager {

    /**
     * Defines the remote root.
     */
    void syncRoot(RemoteFile root);

    /**
     * Sync database
     * @param local the local database
     * @param dbRemoteFile dbRemote file
     * @param factory EntityFactory
     */
    void syncDatabase(IDatabase local, RemoteFile dbRemoteFile, IEntityFactory factory);

    /**
     * Sync now
     */
    void sync();

}
