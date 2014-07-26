package com.bingzer.android.cloudy.providers;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Path;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.IDeleteHistory;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISystemEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;
import com.bingzer.android.driven.LocalFile;

import java.io.File;
import java.io.IOException;

class IncrementProvider extends AbsSyncProvider {

    protected IncrementProvider(ISyncManager manager, IEnvironment remote) {
        super(manager, remote);
    }

    @Override
    public String getName() {
        return "MergeChangesProvider";
    }

    @Override
    protected void doSync() throws SyncException {
        SQLiteSyncBuilder builder = (SQLiteSyncBuilder) local.getDatabase().getBuilder();
        for(ISyncEntity entity : builder.onEntitySynced(local)){
            if(!(entity instanceof ISystemEntity)){
                mergeRemoteChanges(builder, entity);
            }
        }

        // merge history table
        mergeDeleteHistoryTable();

        // We're always going to upload the db
        // So after merging, the remote db is exactly look like a local db

        // copy local db to remote
        // the remote local-file will then be uploaded to the cloud
        copyLocalDb();

        // upload remote db
        uploadRemoteDb();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void mergeRemoteChanges(final SQLiteSyncBuilder builder, final ISyncEntity entity){
        Log.i(getName(), "Merging remote changes: " + entity.getTableName());
        final ITable localTable = local.getDatabase().get(entity.getTableName());
        final ITable remoteTable = remote.getDatabase().get(entity.getTableName());
        final ITable localDelete = local.getDatabase().get(IDeleteHistory.TABLE_NAME);

        // do not sync "Deleted"
        long lastUpdated = manager.getConfig(ILocalConfiguration.LastSync).getValueAsLong();
        remoteTable
                .select("LastUpdated >= ? AND SyncId NOT IN (SELECT EntitySyncId FROM DeleteHistory)", lastUpdated)
                .query(new ISequence<Cursor>() {
            @Override public boolean next(Cursor cursor) {
                entity.load(cursor);

                String condition = builder.onCreateUniqueCondition(entity);
                if(localTable.has(condition)){
                    long localUpdate = localTable.select(condition).query("LastUpdated");
                    if(localUpdate < entity.getLastUpdated())
                        updateLocalEntity(localTable, entity);
                }
                else{
                    // make sure local delete didn't flag this as a deletion
                    if(!localDelete.has("EntitySyncId = ?", entity.getSyncId()))
                        insertLocalEntity(localTable, entity);
                }

                return true;
            }
        });
    }

    private void mergeDeleteHistoryTable(){
        Log.i(getName(), "Merging delete history table");
        final IDeleteHistory deleteHistory = manager.createDeleteHistory(local);
        final ITable localDelete = local.getDatabase().get(IDeleteHistory.TABLE_NAME);
        final ITable remoteDelete = remote.getDatabase().get(IDeleteHistory.TABLE_NAME);

        remoteDelete.select().query(new ISequence<Cursor>() {
            @Override
            public boolean next(Cursor cursor) {
                deleteHistory.load(cursor);
                if (!localDelete.has("SyncId = ?", deleteHistory.getSyncId())){
                    localDelete.insert(deleteHistory);
                    // then delete the entity
                    deleteLocalEntity(deleteHistory);
                }

                return true;
            }
        });
    }

    private void deleteLocalEntity(IDeleteHistory history){
        Log.i(getName(), "# Deleting entity from table " + history.getEntityName());
        try {
            local.getDatabase().get(history.getEntityName()).delete("SyncId = ?", history.getEntitySyncId());
        }
        catch (Exception e){
            Log.e(getName(), "deleteLocalEntity(" + history.getEntityName() + ")", e);
        }
    }

    private void updateLocalEntity(ITable localTable, ISyncEntity entity){
        try{
            localTable.update(entity);
        }
        catch (Exception e){
            Log.e(getName(), "updateLocalEntity(" + localTable.getName() + ", " + entity.getTableName() + ")", e);
        }
    }

    private void insertLocalEntity(ITable localTable, ISyncEntity entity){
        try{
            localTable.insert(entity);
        }
        catch (Exception e){
            Log.e(getName(), "insertLocalEntity(" + localTable.getName() + ", " + entity.getTableName() + ")", e);
        }
    }

    private void copyLocalDb() {
        try{
            Log.i(getName(), "Copying local db to the cache remote db");
            Path.copyFile(new File(local.getDatabase().getPath()), new File(remote.getDatabase().getPath()));
        }
        catch (IOException e){
            throw new SyncException(e);
        }
    }

    private void uploadRemoteDb(){
        Log.i(getName(), "Uploading to the cloud");
        try {
            // upload remote db to cloud to cloud
            LocalFile dbRemoteLocalFile = new LocalFile(new File(remote.getDatabase().getPath()));
            manager.getRemoteDbFile().upload(dbRemoteLocalFile);
        }
        catch (Exception e){
            throw new SyncException(e);
        }
    }
}
