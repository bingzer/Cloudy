package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.SQLiteBuilder;

/**
 * This is the builder that should be used when building
 * the {@link com.bingzer.android.dbv.IDatabase}.
 * This builder will build two additional internal tables
 * inside its {@link #onModelCreate(com.bingzer.android.dbv.IDatabase, com.bingzer.android.dbv.IDatabase.Modeling)}
 * method. Therefore, you should always call
 * {@link super#onModelCreate(com.bingzer.android.dbv.IDatabase, com.bingzer.android.dbv.IDatabase.Modeling)}
 * <p><pre><code>
 * int version = ...;
 * IDatabase dbToSync = ...;
 * dbToSync.open(version, new SQLiteSyncBuilder(getContext()){
 *    ...
 * });
 * </code></pre></p>
 */
public abstract class SQLiteSyncBuilder extends SQLiteBuilder {

    /**
     * Creates an instance of SQLiteSyncBuilder with the default environment
     * {@link com.bingzer.android.dbv.Environment#getLocalEnvironment()}
     */
    protected SQLiteSyncBuilder() {
        super();
    }

    /**
     * Creates an instance of {@code SQLiteSyncBuilder} with a specific
     * environment
     */
    protected SQLiteSyncBuilder(IEnvironment environment) {
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onModelCreate(IDatabase db, IDatabase.Modeling modeling) {
        // -- CloudyClient
        modeling.add(ILocalConfiguration.TABLE_NAME)
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addText("Name", "unique")
                .addText("Value")
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
    public abstract ISyncEntity onEntityCreate(IEnvironment environment, String tableName);

}
