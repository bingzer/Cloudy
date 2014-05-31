package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.ICloudyClient;
import com.bingzer.android.cloudy.contracts.ICloudyHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.SQLiteBuilder;

/**
 * You must use this builder
 */
public abstract class SyncSQLiteBuilder extends SQLiteBuilder {

    protected SyncSQLiteBuilder() {
        super();
    }

    protected SyncSQLiteBuilder(IEnvironment environment) {
        super(environment);
    }

    @Override
    public void onModelCreate(IDatabase db, IDatabase.Modeling modeling) {

        // -- CloudyClient
        modeling.add(ICloudyClient.TABLE_NAME)
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addInteger("ClientId")
                .addInteger("LastSync")
                .ifNotExists();

        // -- CloudySyncHistory
        modeling.add(ICloudyHistory.TABLE_NAME)
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addInteger("Action")
                .addText("Name")
                .addInteger("Timestamp")
                .ifNotExists();

    }

    /**
     * This method will be called when {@link com.bingzer.android.cloudy.SyncManager}
     * requires to create a sample type of {@code ISyncEntity} to populate the data with.
     */
    protected abstract ISyncEntity onEntityCreate(String tableName);

}
