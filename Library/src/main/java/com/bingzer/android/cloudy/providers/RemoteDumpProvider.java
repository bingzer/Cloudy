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
            Counter affected = syncEnvironment(UPSTREAM, range, local, remote);
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


    protected boolean syncSequence(final int streamType, final IEnvironment source, final IEnvironment destination, IEntityHistory syncHistory, Cursor cursor){
        syncHistory.load(cursor);

        SQLiteSyncBuilder builder = (SQLiteSyncBuilder)source.getDatabase().getBuilder();

        IBaseEntity entity = builder.onEntityCreate(source, syncHistory.getEntityName());
        if(!(entity instanceof SyncEntity))
            throw new SyncException("Entity/Table " + entity.getTableName() + " is not an instanceof SyncEntity");
        ISyncEntity syncEntity = (ISyncEntity) entity;

        switch (syncHistory.getEntityAction()) {
            case IEntityHistory.INSERT:
                create(streamType, syncEntity);
                break;
            case IEntityHistory.UPDATE:
                ISyncEntity destEntity = builder.onEntityCreate(destination, syncHistory.getEntityName());
                update(streamType, syncEntity, destEntity);
                break;
            case IEntityHistory.DELETE:
                delete(streamType, syncEntity);
                break;
        }

        return true;
    }
}
