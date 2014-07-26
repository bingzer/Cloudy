package com.bingzer.android.cloudy.providers;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;

import java.io.File;

/**
 * This is the provider when the remote has an empty (no database) yet
 */
class RemoteDumpProvider extends AbsSyncProvider {

    protected RemoteDumpProvider(ISyncManager manager, IEnvironment remote) {
        super(manager, remote);
    }

    @Override
    public String getName() {
        return "RemoteDumpProvider";
    }

    @Override
    protected void doSync() throws SyncException {
        LocalFile dbRemoteLocalFile = new LocalFile(new File(remote.getDatabase().getPath()));
        manager.getRemoteDbFile().upload(dbRemoteLocalFile);
    }
}
