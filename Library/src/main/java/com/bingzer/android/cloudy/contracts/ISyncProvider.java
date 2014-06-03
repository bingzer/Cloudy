package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;

public interface ISyncProvider {

    long sync(long lastTimestamp) throws SyncException;

    void cleanup();

}
