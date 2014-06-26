package com.bingzer.android.cloudy.providers;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ISyncProvider;

class LocalDumpProvider implements ISyncProvider{

    @Override
    public long sync(long lastTimestamp) throws SyncException {
        return 0;
    }

    @Override
    public void cleanup() {

    }
}
