package com.bingzer.android.cloudy.contracts;

public interface IClientRevision extends ISystemEntity {
    String TABLE_NAME = "ClientRevision";

    void setClientId(long clientId);

    long getClientId();

    long getRevision();

    void setRevision(long timestamp);

}
