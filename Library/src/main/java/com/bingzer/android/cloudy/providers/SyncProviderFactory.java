package com.bingzer.android.cloudy.providers;

import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;

public final class SyncProviderFactory {

    public static ISyncProvider getSyncProvider(ISyncManager manager, int syncType){
        switch (syncType){
            case ISyncManager.SYNC_INCREMENT: return new IncrementProvider(manager);
            case ISyncManager.SYNC_DUMP_TO_REMOTE: return new RemoteDumpProvider(manager);
        }

        throw new UnsupportedOperationException("Not supported sync type: " + syncType);
    }
}
