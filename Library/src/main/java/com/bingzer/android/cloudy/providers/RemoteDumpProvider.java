package com.bingzer.android.cloudy.providers;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.SyncEntity;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.IBaseEntity;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;

import java.io.File;
import java.io.IOException;

/**
 * This is the provider when the remote has an empty (no database) yet
 */
class RemoteDumpProvider extends AbsSyncProvider {

    protected RemoteDumpProvider(ISyncManager manager) {
        super(manager);
    }

    @Override
    protected String getName() {
        return "RemoteDumpProvider";
    }

    @Override
    public long sync(long timestamp) {
        Log.i(getName(), "Sync starting. Revision: " + timestamp);
        try{
            TimeRange range = new TimeRange(timestamp, Timespan.now());
            Counter counter = new Counter();

            // upload to remote
            File remoteDbFile = new File(remote.getDatabase().getPath());
            Path.copyFile(new File(local.getDatabase().getPath()), remoteDbFile);
            remote.getDatabase().open(local.getDatabase().getVersion(),
                    remoteDbFile.getAbsolutePath(),
                    new SQLiteSyncBuilder.Copy((SQLiteSyncBuilder) local.getDatabase().getBuilder()));

            // Entity (Local to Remote)
            Counter affected = syncLocalFilesToRemote();
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter LocalToRemote(Entity) = " + counter.value);

            Log.i(getName(), "Total SyncCounter = " + counter.value);

            return range.to;
        }
        catch (IOException e){
            Log.e("Sync()", e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            Log.i(getName(), "End of sync()");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    protected Counter syncLocalFilesToRemote(){
        final IEntityHistory syncHistory = manager.createEntityHistory(remote);
        final Counter counter = new Counter();
        local.getDatabase().get(IEntityHistory.TABLE_NAME).select()
                .orderBy("Timestamp")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        counter.value++;
                        return syncSequence(syncHistory, cursor);
                    }
                });
        return counter;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private boolean syncSequence(IEntityHistory syncHistory, Cursor cursor){
        syncHistory.load(cursor);

        SQLiteSyncBuilder builder = (SQLiteSyncBuilder) remote.getDatabase().getBuilder();
        ITable localTable = local.getDatabase().get(syncHistory.getEntityName());
        ITable remoteTable = remote.getDatabase().get(syncHistory.getEntityName());

        IBaseEntity entity = builder.onEntityCreate(local, syncHistory.getEntityName());
        if(!(entity instanceof SyncEntity))
            throw new SyncException("Entity/Table " + entity.getTableName() + " is not an instanceof SyncEntity");
        ISyncEntity syncEntity = (ISyncEntity) entity;

        switch (syncHistory.getEntityAction()) {
            case IEntityHistory.INSERT:
                if(localTable.has("SyncId = ?", syncHistory.getEntitySyncId())) {
                    localTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);
                    create(UPSTREAM, syncEntity);
                }
                break;
            case IEntityHistory.UPDATE:
                update(UPSTREAM, syncEntity, null);
                break;
            case IEntityHistory.DELETE:
                delete(UPSTREAM, syncEntity);
                break;
        }

        return true;
    }
}
