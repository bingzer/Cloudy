package com.bingzer.android.cloudy.contracts;

public interface IEntityHistory extends ISystemEntity {

    String TABLE_NAME = "CloudyHistory";

    int INSERT = 0;
    int DELETE = 1;
    int UPDATE = 2;


    /**
     * What type of actions? INSERT, DELETE or UPDATE
     */
    int getEntityAction();

    /**
     * Actor table name
     */
    String getEntityName();

    /**
     * Actor's sync id
     */
    long getEntitySyncId();

    /**
     * WHEN
     */
    long getTimestamp();

}
