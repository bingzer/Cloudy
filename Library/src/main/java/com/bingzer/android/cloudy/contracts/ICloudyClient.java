package com.bingzer.android.cloudy.contracts;


public interface ICloudyClient extends ISystemEntity {
    String TABLE_NAME = "CloudyClient";


    long getClientId();

    long getLastSync();

    void setLastSync(long timestamp);

}
