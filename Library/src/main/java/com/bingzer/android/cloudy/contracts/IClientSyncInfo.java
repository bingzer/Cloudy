package com.bingzer.android.cloudy.contracts;

public interface IClientSyncInfo extends ISystemEntity {
    String TABLE_NAME = "CloudyClient";

    void setClientId(long clientId);

    long getClientId();

    long getRevision();

    void setRevision(long timestamp);

}
