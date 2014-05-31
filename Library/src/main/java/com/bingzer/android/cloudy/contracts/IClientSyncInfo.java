package com.bingzer.android.cloudy.contracts;

public interface IClientSyncInfo extends ISystemEntity {
    String TABLE_NAME = "CloudyClient";


    long getClientId();

    long getLastSync();

    void setLastSync(long timestamp);

}
