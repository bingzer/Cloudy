package com.bingzer.android.cloudy.providers;

import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.IEnvironment;

public final class SyncProviderFactory {
    public static ISyncProvider getSyncProvider(ISyncManager manager, IEnvironment remote, int syncType){
        switch (syncType){
            case ISyncManager.SYNC_INCREMENT: return new IncrementProvider(manager, remote);
            case ISyncManager.SYNC_DUMP_TO_REMOTE: return new RemoteDumpProvider(manager, remote);
            case ISyncManager.SYNC_DUMP_TO_LOCAL: return new LocalDumpProvider(manager, remote);
            case ISyncManager.SYNC_FILES: return new DirectorySyncProvider(manager, remote);
        }

        throw new UnsupportedOperationException("Not supported sync type: " + syncType);
    }
}
