package com.bingzer.android.cloudy;

import android.database.Cursor;
import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ICloudyClient;
import com.bingzer.android.cloudy.contracts.ICloudyHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.ITable;
import com.bingzer.android.dbv.contracts.IBaseEntity;
import com.bingzer.android.dbv.contracts.IEnvironment;
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

    public SyncProvider(ISyncManager manager, IEnvironment local, IEnvironment remote){
        this.manager = manager;
        this.local = local;
        this.remote = remote;
    }

    @Override
    public void sync(long timestamp) {
        Log.i(TAG, "Sync starting. LastSync: " + timestamp);

        // update Local to Remote
        updateEnvironment(UPSTREAM, timestamp, local, remote);
        // update Remote to Local
        updateEnvironment(DOWNSTREAM, timestamp, remote, local);

        // update sync history
        updateCloudHistory(UPSTREAM, timestamp, local, remote);
        updateCloudHistory(DOWNSTREAM, timestamp, remote, local);

        // update client data
        long now = Timespan.now();
        updateCloudClient(now, local);
        updateCloudClient(now, remote);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void updateEnvironment(final int streamType, long timestamp, final IEnvironment source, final IEnvironment target){
        final ICloudyHistory syncHistory = new CloudyHistory(target);
        source.getDatabase().get(ICloudyHistory.TABLE_NAME).select("Timestamp > ?", timestamp)
                .orderBy("Timestamp DESC")
                .query(new ISequence<Cursor>() {
                    @Override
                    public boolean next(Cursor cursor) {
                        return syncSequence(streamType, source, target, syncHistory, cursor);
                    }
                });
    }

    private void updateCloudClient(long timestamp, final IEnvironment source){
        ICloudyClient client = CloudyClient.getClient(source, manager.getClientId());
        client.setLastSync(timestamp);
        client.save();
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private boolean syncSequence(final int streamType, final IEnvironment source, final IEnvironment target, ICloudyHistory syncHistory, Cursor cursor){
        syncHistory.load(cursor);

        ITable localTable = source.getDatabase().get(syncHistory.getName());
        ITable remoteTable = target.getDatabase().get(syncHistory.getName());
        IBaseEntity entity = target.getEntityFactory().createEntity(syncHistory.getName());
        if(entity instanceof SyncEntity)
            throw new SyncException("Entity/Table " + entity.getTableName() + " is not an instanceof SyncEntity");
        ISyncEntity syncEntity = (ISyncEntity) entity;

        switch (syncHistory.getAction()) {
            case ICloudyHistory.INSERTED:
                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(syncEntity);
                remoteTable.insert(syncEntity);

                create(streamType, syncEntity);
                break;
            case ICloudyHistory.DELETED:
                remoteTable.delete("SyncID = ?", syncHistory.getSyncId());

                delete(streamType, syncEntity);
                break;
            case ICloudyHistory.UPDATED:
                localTable.select("SyncId = ?", syncHistory.getSyncId()).query(syncEntity);
                remoteTable.update(syncEntity);

                update(streamType, syncEntity);
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

    private void update(int streamType, ISyncEntity entity){
        switch (streamType){
            default:
                throw new SyncException("Unknown stream type: " + streamType);
            case UPSTREAM:
                updateUpstream(entity);
                break;
            case DOWNSTREAM:
                updateDownstream(entity);
                break;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void createUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                String filename = createRemoteFileNameForLocalFile(entity, file);
                remoteDir.create(filename, localFile);
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
                remoteFile.download(localFile);
            }
        }
    }

    private void deleteUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                String filename = createRemoteFileNameForLocalFile(entity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                remoteFile.delete();
            }
        }
    }

    private void deleteDownstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){

            for(File file : entity.getLocalFiles()){
                if(!file.delete())
                    throw new SyncException("Failed to delete: " + file);
            }
        }
    }

    private void updateUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                String filename = createRemoteFileNameForLocalFile(entity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                remoteFile.upload(localFile);
            }
        }
    }

    private void updateDownstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                String filename = createRemoteFileNameForLocalFile(entity, file);
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
        return entity.getSyncId() + ":" + file.getName();
    }

    private void updateCloudHistory(int type, long timestamp, final IEnvironment source, final IEnvironment target){
        final ICloudyHistory syncHistory = new CloudyHistory(target);
        source.getDatabase().get(ICloudyHistory.TABLE_NAME).select("Timestamp > ?", timestamp)
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

    private boolean hasLocalFiles(ISyncEntity entity){
        return entity.getLocalFiles() != null && entity.getLocalFiles().length > 0;
    }

}
