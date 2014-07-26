package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IBaseEntity;

/**
 * Represents a contract for any entity that is capable
 * of syncing. Every {@link com.bingzer.android.cloudy.contracts.ISyncEntity}
 * requires a {@code syncId} that represents a unique id
 * that is used by both the local Database and remote Database
 */
public interface ISyncEntity extends IBaseEntity {

    /**
     * The sync id. Which is unique. SyncId is automatically
     * generated. No need to set this
     * @param syncId the sync unique identifier
     */
    void setSyncId(long syncId);

    /**
     * Returns the sync unique identifier.
     */
    long getSyncId();

    void setLastUpdated(long lastUpdated);

    long getLastUpdated();

    /**
     * Loads by its sync id.
     */
    boolean loadBySyncId(long syncId);
}
