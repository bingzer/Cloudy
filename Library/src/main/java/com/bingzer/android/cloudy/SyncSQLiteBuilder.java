package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.ICloudyClient;
import com.bingzer.android.cloudy.contracts.ICloudyHistory;
import com.bingzer.android.cloudy.contracts.IEntityFactory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.entities.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.SQLiteBuilder;

/**
 * You must use this builder
 */
public abstract class SyncSQLiteBuilder extends SQLiteBuilder{

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

    @Override
    public void onReady(IDatabase database) {
        IEnvironment local = Environment.getLocalEnvironment();
        local.setDatabase(database);
        local.setEntityFactory(getEntityFactory());

        super.onReady(database);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public abstract IEntityFactory getEntityFactory();
}
