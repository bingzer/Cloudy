package com.bingzer.android.cloudy.contracts;

/**
 * Table that recorded all entity history.
 * {@code ISyncEntity} is tracked whenever
 * {@code UPDATE}, {@code DELETE} or {@code INSERT}
 * is performed by calling
 * {@link com.bingzer.android.cloudy.contracts.ISyncEntity#save()}
 */
public interface IEntityHistory extends ISystemEntity {

    /**
     * Table name
     */
    String TABLE_NAME = "EntityHistory";

    /**
     * INSERT operation
     */
    int INSERT = 0;

    /**
     * DELETE operation
     */
    int DELETE = 1;

    /**
     * UPDATE oepration
     */
    int UPDATE = 2;

    //////////////////////////////////////////////////////////////////

    /**
     * What type of actions? INSERT, DELETE or UPDATE
     * @see #INSERT
     * @see #UPDATE
     * @see #DELETE
     */
    int getEntityAction();

    /**
     * Actor table name.
     * This will be the table in which the action performed
     */
    String getEntityName();

    /**
     * Actor's sync id.
     */
    long getEntitySyncId();

    /**
     * WHEN the action is performed
     */
    long getTimestamp();

}
