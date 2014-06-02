package com.bingzer.android.cloudy.contracts;

public interface ISyncProvider {

    long sync(long lastTimestamp);

    void cleanup();

}
