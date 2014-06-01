package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.SQLiteBuilder;

/**
 * You must use this builder
 */
public abstract class SQLiteSyncBuilder extends SQLiteBuilder {

    protected SQLiteSyncBuilder() {
        super();
    }

    protected SQLiteSyncBuilder(IEnvironment environment) {
        super(environment);
    }

    @Override
    public void onModelCreate(IDatabase db, IDatabase.Modeling modeling) {
        // -- CloudyClient
        modeling.add(IClientSyncInfo.TABLE_NAME)
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addInteger("ClientId")
                .addInteger("LastSync")
                .index("Id", "SyncId")
                .ifNotExists();

        // -- CloudySyncHistory
        modeling.add(IEntityHistory.TABLE_NAME)
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addInteger("EntityAction")
                .addInteger("EntitySyncId")
                .addText("EntityName")
                .addInteger("Timestamp")
                .index("Timestamp", "SyncId", "Id")
                .ifNotExists();
    }

    /**
     * This method will be called when {@link SQLiteSyncManager}
     * requires to create a sample type of {@code ISyncEntity} to populate the data with.
     */
    protected abstract ISyncEntity onEntityCreate(IEnvironment environment, String tableName);

}
