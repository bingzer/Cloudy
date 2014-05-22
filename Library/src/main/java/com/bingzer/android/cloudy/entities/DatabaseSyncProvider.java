package com.bingzer.android.cloudy.entities;

import android.database.Cursor;

import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.contracts.IBaseEntity;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.contracts.SyncProvider;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;

public class DatabaseSyncProvider implements SyncProvider {

    private SyncEnvironment remote;
    private SyncEnvironment local;

    public DatabaseSyncProvider(EntityFactory factory, IDatabase remoteDb, IDatabase localDb){
        remote = new SyncEnvironment(remoteDb, factory);
        local = new SyncEnvironment(localDb, factory);
    }

    @Override
    public void sync(long timestamp) {
        // update Local to Remote
        updateEnvironment(timestamp, local, remote);
        // update Remote to Local
        updateEnvironment(timestamp, remote, local);

        // update sync history
        updateSyncHistory(timestamp, local, remote);
        updateSyncHistory(timestamp, remote, local);

        // update sync data
        updateSyncData(timestamp, local);
        updateSyncData(timestamp, remote);
    }

    private void updateEnvironment(long timestamp, final IEnvironment source, final IEnvironment target){
        final SyncHistory syncHistory = new SyncHistory(source);
        source.getSyncHistory().select("Timestamp > ?", timestamp)
                .orderBy("Timestamp DESC")
                .query(new ISequence<Cursor>() {

                    @Override
                    public boolean next(Cursor cursor) {
                        syncHistory.load(cursor);
                        final ITable localTable = source.getDatabase().get(syncHistory.getName());
                        final ITable remoteTable = target.getDatabase().get(syncHistory.getName());
                        final IBaseEntity entity = target.getEntityFactory().createEntity(syncHistory.getName());

                        switch (syncHistory.getAction()) {
                            case SyncHistory.INSERTED:
                                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(entity);
                                remoteTable.insert(entity);
                                break;
                            case SyncHistory.DELETED:
                                remoteTable.delete("SyncID = ?", syncHistory.getSyncId());
                                break;
                            case SyncHistory.UPDATED:
                                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(entity);
                                remoteTable.update(entity);
                                break;
                        }

                        return true;
                    }
                });
    }

    private void updateSyncHistory(long timestamp, final IEnvironment source, final IEnvironment target){
        final SyncHistory syncHistory = new SyncHistory(source);
        source.getSyncHistory().select("Timestamp > ?", timestamp)
                .orderBy("Timestamp DESC")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        syncHistory.load(cursor);
                        target.getSyncHistory().insert(syncHistory);

                        return true;
                    }
                });
    }

    private void updateSyncData(long timestamp, final IEnvironment source){
        source.getSyncData().update("Name = ?", SyncData.SYNC_DATE).val("Value", timestamp);
    }

}
