package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;

public interface ISyncProvider {
    int UPSTREAM = 1;
    int DOWNSTREAM = 2;

    //////////////////////////////////////////////////////////////////////////////////////////////

    long sync(long timestamp) throws SyncException;

    void cleanup();

}
