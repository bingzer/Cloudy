package com.bingzer.android.cloudy.providers;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.queries.ISequence;

class LocalDumpProvider extends AbsSyncProvider{

    protected LocalDumpProvider(ISyncManager manager) {
        super(manager);
    }

    @Override
    protected String getName() {
        return "LocalDumpProvider";
    }

    @Override
    public long sync(long timestamp) throws SyncException {
        Log.i(getName(), "Sync starting. Revision: " + timestamp);
        try{
            TimeRange range = new TimeRange(timestamp, Timespan.now());
            Counter counter = new Counter();

            // Entity (Remote to Local)
            Counter affected = syncRemoteFilesToRemote();
            counter.value += affected.value;
            Log.d(getName(), "SyncCounter RemoteToLocal(Entity) = " + counter.value);

            Log.i(getName(), "Total SyncCounter = " + counter.value);

            return range.to;
        }
        finally {
            Log.i(getName(), "End of sync()");
        }
    }

    protected Counter syncRemoteFilesToRemote(){
        final IEntityHistory syncHistory = manager.createEntityHistory(local);
        final Counter counter = new Counter();
        remote.getDatabase().get(IEntityHistory.TABLE_NAME).select()
                .orderBy("Timestamp")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        counter.value++;
                        syncHistory.load(cursor);

                        SQLiteSyncBuilder builder = (SQLiteSyncBuilder) local.getDatabase().getBuilder();
                        ISyncEntity syncEntity = builder.onEntityCreate(local, syncHistory.getTableName());

                        switch (syncHistory.getEntityAction()) {
                            case IEntityHistory.INSERT:
                                create(DOWNSTREAM, syncEntity);
                                break;
                            case IEntityHistory.UPDATE:
                                update(DOWNSTREAM, syncEntity, null);
                                break;
                            case IEntityHistory.DELETE:
                                delete(DOWNSTREAM, syncEntity);
                                break;
                        }

                        return true;
                    }
                });
        return counter;
    }
}
