package com.bingzer.android.cloudy.providers;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.queries.ISequence;

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

            // Entity (Local to Remote)
            Counter affected = syncLocalFilesToRemote();
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter LocalToRemote(Entity) = " + counter.value);

            Log.i(getName(), "Total SyncCounter = " + counter.value);

            return range.to;
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
                        syncHistory.load(cursor);

                        SQLiteSyncBuilder builder = (SQLiteSyncBuilder) remote.getDatabase().getBuilder();
                        ISyncEntity syncEntity = builder.onEntityCreate(remote, syncHistory.getTableName());

                        switch (syncHistory.getEntityAction()) {
                            case IEntityHistory.INSERT:
                                create(UPSTREAM, syncEntity);
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
                });
        return counter;
    }
}
