package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;

public interface ISyncProvider {

    String getName();

    void sync() throws SyncException;

    void close() throws SyncException;

}
