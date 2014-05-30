package com.bingzer.android.cloudy.contracts;

public interface ICloudyHistory extends ISystemEntity {

    String TABLE_NAME = "CloudyHistory";

    int INSERTED = 0;
    int DELETED = 1;
    int UPDATED = 2;


    int getAction();

    String getName();

    long getTimestamp();

}
