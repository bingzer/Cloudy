package com.bingzer.android.cloudy.contracts;

public interface IDataHistory extends ISystemEntity {

    String TABLE_NAME = "DataHistory";
    int INSERTED = 0;
    int DELETED = 1;
    int UPDATED = 2;


    int getAction();

    String getName();

    long getTimestamp();

}
