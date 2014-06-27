package com.bingzer.android.cloudy.providers;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.SyncEntity;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.IBaseEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;

class IncrementProvider extends AbsSyncProvider {

    IncrementProvider(ISyncManager manager){
        super(manager);
    }

    @Override
    protected String getName() {
        return "IncrementProvider";
    }

    @Override
    public long sync(long timestamp) {
        Log.i(getName(), "Sync starting. Revision: " + timestamp);
        try{
            TimeRange range = new TimeRange(timestamp, Timespan.now());
            Counter counter = new Counter();

            // Entity (Local to Remote)
            Counter affected = syncEnvironment(UPSTREAM, range, local, remote);
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter LocalToRemote(Entity) = " + counter.value);

            // Entity (Remote to Local)
            affected = syncEnvironment(DOWNSTREAM, range, remote, local);
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter RemoteToLocal(Entity) = " + counter.value);

            // EntityHistory (Local to Remote)
            affected = syncEntityHistory(range, local, remote);
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter LocalToRemote(EntityHistory) = " + counter.value);

            // EntityHistory (Remote to Local)
            affected = syncEntityHistory(range, remote, local);
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter RemoteToLocal(EntityHistory) = " + counter.value);

            Log.i(getName(), "Total SyncCounter = " + counter.value);

            return range.to;
        }
        finally {
            Log.i(getName(), "End of sync()");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    protected Counter syncEnvironment(final int streamType, TimeRange range, final IEnvironment source, final IEnvironment destination){
        final Counter counter = new Counter();
        final IEntityHistory syncHistory = manager.createEntityHistory(destination);
        source.getDatabase().get(IEntityHistory.TABLE_NAME).select("Timestamp >= ? AND Timestamp < ?", range.from, range.to)
                .orderBy("Timestamp")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        counter.value ++;
                        return syncSequence(streamType, source, destination, syncHistory, cursor);
                    }
                });
        return counter;
    }

    protected Counter syncEntityHistory(TimeRange range, final IEnvironment source, final IEnvironment destination){
        final Counter counter = new Counter();
        final IEntityHistory syncHistory = manager.createEntityHistory(destination);
        source.getDatabase().get(IEntityHistory.TABLE_NAME).select("Timestamp >= ? AND Timestamp < ?", range.from, range.to)
                .orderBy("Timestamp")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        counter.value++;

                        syncHistory.load(cursor);
                        if (!destination.getDatabase()
                                .get(IEntityHistory.TABLE_NAME)
                                .has("SyncId = ?", syncHistory.getSyncId())) {
                            syncHistory.setId(-1);
                            syncHistory.save();
                        }
                        return true;
                    }
                });
        return counter;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    protected boolean syncSequence(final int streamType, final IEnvironment source, final IEnvironment destination, IEntityHistory syncHistory, Cursor cursor){
        syncHistory.load(cursor);

        SQLiteSyncBuilder builder = (SQLiteSyncBuilder)source.getDatabase().getBuilder();

        ITable sourceTable = source.getDatabase().get(syncHistory.getEntityName());
        ITable destinationTable = destination.getDatabase().get(syncHistory.getEntityName());

        IBaseEntity entity = builder.onEntityCreate(source, syncHistory.getEntityName());
        if(!(entity instanceof SyncEntity))
            throw new SyncException("Entity/Table " + entity.getTableName() + " is not an instanceof SyncEntity");
        ISyncEntity syncEntity = (ISyncEntity) entity;

        switch (syncHistory.getEntityAction()) {
            case IEntityHistory.INSERT:
                // only insert if it exists on the source table
                // the record may be removed
                if(sourceTable.has("SyncId = ?", syncHistory.getEntitySyncId())) {
                    sourceTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);

                    // then check if the destination table already has this entity
                    if(!destinationTable.has("SyncId = ?", syncEntity.getSyncId())) {
                        destinationTable.insert(syncEntity);
                        create(streamType, syncEntity);
                    }
                }
                break;
            case IEntityHistory.UPDATE:
                if(sourceTable.has("SyncId = ?", syncHistory.getEntitySyncId())){
                    sourceTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);

                    ISyncEntity destEntity = builder.onEntityCreate(destination, syncHistory.getEntityName());
                    if(destinationTable.has("SyncId = ?", syncHistory.getEntitySyncId())){
                        destinationTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(destEntity);

                        update(streamType, syncEntity, destEntity);
                        destinationTable.update(syncEntity);
                    }
                }
                break;
            case IEntityHistory.DELETE:
                destinationTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);
                destinationTable.delete("SyncId = ?", syncHistory.getEntitySyncId());

                delete(streamType, syncEntity);
                break;
        }

        return true;
    }

}
