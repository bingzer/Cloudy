package com.bingzer.android.cloudy;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.IBaseEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.queries.ISequence;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;

class SyncProvider implements ISyncProvider {

    static final String TAG = "SyncProvider";
    static final int UPSTREAM = 1;
    static final int DOWNSTREAM = 2;

    private IEnvironment remote;
    private IEnvironment local;
    private ISyncManager manager;

    SyncProvider(ISyncManager manager, IEnvironment local, IEnvironment remote){
        this.manager = manager;
        this.local = local;
        this.remote = remote;
    }

    @Override
    public long sync(long timestamp) {
        Log.i(TAG, "Sync starting. Revision: " + timestamp);
        try{
            TimeRange range = new TimeRange(timestamp, Timespan.now());
            Counter counter = new Counter();

            // Entity (Local to Remote)
            Counter affected = syncEnvironment(UPSTREAM, range, local, remote);
            counter.value += affected.value;
            Log.d(TAG, "SyncCounter LocalToRemote(Entity) = " + counter.value);

            // Entity (Remote to Local)
            affected = syncEnvironment(DOWNSTREAM, range, remote, local);
            counter.value += affected.value;
            Log.d(TAG, "SyncCounter RemoteToLocal(Entity) = " + counter.value);

            // EntityHistory (Local to Remote)
            affected = syncEntityHistory(range, local, remote);
            counter.value += affected.value;
            Log.d(TAG, "SyncCounter LocalToRemote(EntityHistory) = " + counter.value);

            // EntityHistory (Remote to Local)
            affected = syncEntityHistory(range, remote, local);
            counter.value += affected.value;
            Log.d(TAG, "SyncCounter RemoteToLocal(EntityHistory) = " + counter.value);

            // Client REVISION (Local only)
            Log.d(TAG, "Updating LocalConfiguration's Revision to: " + range.to);
            ILocalConfiguration config = LocalConfiguration.getConfig(local, LocalConfiguration.SETTING_CLIENTID);
            config.setValue(range.to);
            config.save();

            Log.i(TAG, "Total SyncCounter = " + counter.value);

            return range.to;
        }
        finally {
            Log.i(TAG, "End of sync()");
        }
    }

    @Override
    public void cleanup() {
        File db = new File(remote.getDatabase().getPath());
        // cleanup
        remote.getDatabase().close();
        if(!db.delete())
            Log.e(TAG, "Cleanup() - failed to delete remote db");
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private Counter syncEnvironment(final int streamType, TimeRange range, final IEnvironment source, final IEnvironment destination){
        final Counter counter = new Counter();
        final IEntityHistory syncHistory = new EntityHistory(destination);
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

    private Counter syncEntityHistory(TimeRange range, final IEnvironment source, final IEnvironment destination){
        final Counter counter = new Counter();
        final IEntityHistory syncHistory = new EntityHistory(destination);
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

    private boolean syncSequence(final int streamType, final IEnvironment source, final IEnvironment destination, IEntityHistory syncHistory, Cursor cursor){
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
                sourceTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);
                destinationTable.insert(syncEntity);

                create(streamType, syncEntity);
                break;
            case IEntityHistory.DELETE:
                destinationTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);
                destinationTable.delete("SyncId = ?", syncHistory.getEntitySyncId());

                delete(streamType, syncEntity);
                break;
            case IEntityHistory.UPDATE:
                sourceTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(syncEntity);

                ISyncEntity destEntity = builder.onEntityCreate(destination, syncHistory.getEntityName());
                destinationTable.select("SyncId = ?", syncHistory.getEntitySyncId()).query(destEntity);

                update(streamType, syncEntity, destEntity);
                destinationTable.update(syncEntity);
                break;
        }

        return true;
    }

    private void create(int streamType, ISyncEntity entity){
        switch (streamType){
            default:
                throw new SyncException("Unknown stream type: " + streamType);
            case UPSTREAM:
                createUpstream(entity);
                break;
            case DOWNSTREAM:
                createDownstream(entity);
                break;
        }
    }

    private void delete(int streamType, ISyncEntity entity){
        switch (streamType){
            default:
                throw new SyncException("Unknown stream type: " + streamType);
            case UPSTREAM:
                deleteUpstream(entity);
                break;
            case DOWNSTREAM:
                deleteDownstream(entity);
                break;
        }
    }

    private void update(int streamType, ISyncEntity srcEntity, ISyncEntity destEntity){
        switch (streamType){
            default:
                throw new SyncException("Unknown stream type: " + streamType);
            case UPSTREAM:
                updateUpstream(srcEntity, destEntity);
                break;
            case DOWNSTREAM:
                updateDownstream(srcEntity, destEntity);
                break;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void createUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                localFile.setName(createRemoteFileNameForLocalFile(entity, file));
                RemoteFile remoteFile = remoteDir.create(localFile);
                if(remoteFile == null)
                    throw new SyncException("Unable to create RemoteFile: " + localFile.getName());
            }
        }
    }

    private void createDownstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                String filename = createRemoteFileNameForLocalFile(entity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                if(!remoteFile.download(localFile))
                    throw new SyncException("Unable to download: " + file.getName());
            }
        }
    }

    private void deleteUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                String filename = createRemoteFileNameForLocalFile(entity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                if(!remoteFile.delete())
                    throw new SyncException("Unable to delete: " + file.getName());
            }
        }
    }

    private void deleteDownstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){

            for(File file : entity.getLocalFiles()){
                if(!file.delete())
                    throw new SyncException("Unable to delete: " + file);
            }
        }
    }

    private void updateUpstream(ISyncEntity srcEntity, ISyncEntity destEntity){
        if(hasLocalFiles(destEntity)){
            RemoteFile remoteDir = getRemoteDirectory(destEntity);

            for(File file : destEntity.getLocalFiles()){
                String filename = createRemoteFileNameForLocalFile(destEntity, file);
                remoteDir.get(filename).delete();
            }
        }

        if(hasLocalFiles(srcEntity)){
            RemoteFile remoteDir = getRemoteDirectory(srcEntity);

            for(File file : srcEntity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                localFile.setName(createRemoteFileNameForLocalFile(srcEntity, file));
                remoteDir.create(localFile);
            }
        }
    }

    private void updateDownstream(ISyncEntity srcEntity, ISyncEntity destEntity){
        if(hasLocalFiles(destEntity)){
            for(File file : destEntity.getLocalFiles()){
                if(!file.delete()){
                    Log.w(TAG, "Unable to delete " + file);
                }
            }
        }

        if(hasLocalFiles(srcEntity)){
            RemoteFile remoteDir = getRemoteDirectory(srcEntity);

            for(File file : srcEntity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                String filename = createRemoteFileNameForLocalFile(srcEntity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                remoteFile.download(localFile);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private RemoteFile getRemoteDirectory(ISyncEntity entity){
        RemoteFile remoteFile = manager.getRoot().get(entity.getTableName());
        if(remoteFile == null)
            remoteFile = manager.getRoot().create(entity.getTableName());

        if(remoteFile == null)
            throw new SyncException("Can't get or create remote directory for " + entity.getTableName());
        return remoteFile;
    }

    private String createRemoteFileNameForLocalFile(ISyncEntity entity, File file){
        return entity.getSyncId() + "." + file.getName();
    }

    private boolean hasLocalFiles(ISyncEntity entity){
        return entity.getLocalFiles() != null && entity.getLocalFiles().length > 0;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////

    private static class TimeRange {
        long from;
        long to;
        TimeRange(long from, long to){
            this.from = from;
            this.to = to;
        }
    }

    private static class Counter {
        long value = 0;
    }
}
