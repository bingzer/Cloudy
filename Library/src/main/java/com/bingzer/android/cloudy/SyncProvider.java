package com.bingzer.android.cloudy;

import android.database.Cursor;

import com.bingzer.android.cloudy.contracts.IBaseEntity;
import com.bingzer.android.cloudy.contracts.IDataHistory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;

class SyncProvider implements ISyncProvider {

    private IEnvironment remote;
    private IEnvironment local;

    public SyncProvider(IEnvironment local, IEnvironment remote){
        this.local = local;
        this.remote = remote;
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
        final IDataHistory syncHistory = target.createDataHistory();
        source.getDatabase().get(IDataHistory.TABLE_NAME).select("Timestamp > ?", timestamp)
                .orderBy("Timestamp DESC")
                .query(new ISequence<Cursor>() {

                    @Override
                    public boolean next(Cursor cursor) {
                        syncHistory.load(cursor);
                        final ITable localTable = source.getDatabase().get(syncHistory.getName());
                        final ITable remoteTable = target.getDatabase().get(syncHistory.getName());
                        final IBaseEntity entity = target.getEntityFactory().createEntity(syncHistory.getName());

                        switch (syncHistory.getAction()) {
                            case IDataHistory.INSERTED:
                                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(entity);
                                remoteTable.insert(entity);
                                break;
                            case IDataHistory.DELETED:
                                remoteTable.delete("SyncID = ?", syncHistory.getSyncId());
                                break;
                            case IDataHistory.UPDATED:
                                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(entity);
                                remoteTable.update(entity);
                                break;
                        }

                        return true;
                    }
                });
    }

    private void updateSyncHistory(long timestamp, final IEnvironment source, final IEnvironment target){
        final IDataHistory syncHistory = target.createDataHistory();
        source.getDatabase().get(IDataHistory.TABLE_NAME).select("Timestamp > ?", timestamp)
                .orderBy("Timestamp DESC")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        syncHistory.load(cursor);
                        syncHistory.save();

                        return true;
                    }
                });
    }

    private void updateSyncData(long timestamp, final IEnvironment source){
        //final IDataEntity syncEntity = source.createDataEntity();
        //source.getSyncData().update("Name = ?", SyncData.SYNC_DATE).val("Value", timestamp);
    }

}
