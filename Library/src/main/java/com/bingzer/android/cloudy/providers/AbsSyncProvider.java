package com.bingzer.android.cloudy.providers;

import android.util.Log;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;

abstract class AbsSyncProvider implements ISyncProvider{
    protected final IEnvironment remote;
    protected final IEnvironment local;
    protected final ISyncManager manager;

    protected AbsSyncProvider(ISyncManager manager){
        this.manager = manager;
        this.local = manager.getLocalEnvironment();
        this.remote = manager.getRemoteEnvironment();
    }

    @Override
    public void close() {
        File db = new File(remote.getDatabase().getPath());
        // close
        remote.getDatabase().close();
        if(!db.delete())
            Log.e(getName(), "Cleanup() - failed to delete remote db");
    }

    protected abstract String getName();

    /////////////////////////////////////////////////////////////////////////////////////////

    protected void create(int streamType, ISyncEntity entity){
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

    protected void delete(int streamType, ISyncEntity entity){
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

    protected void update(int streamType, ISyncEntity srcEntity, ISyncEntity destEntity){
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

                RemoteFile remoteFile = remoteDir.get(localFile.getName());
                if(remoteFile != null)
                    remoteFile.upload(localFile);
                else
                    remoteFile = remoteDir.create(localFile);

                if(remoteFile == null) {
                    Log.e(getName(), "Unable to create RemoteFile: " + localFile.getName());
                }
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
                if(!remoteFile.download(localFile)) {
                    Log.e(getName(), "Unable to download: " + file.getName());
                }
            }
        }
    }

    private void deleteUpstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){
            RemoteFile remoteDir = getRemoteDirectory(entity);

            for(File file : entity.getLocalFiles()){
                String filename = createRemoteFileNameForLocalFile(entity, file);
                RemoteFile remoteFile = remoteDir.get(filename);
                if(remoteFile != null && !remoteFile.delete())
                    Log.e(getName(), "Unable to delete: " + file.getName());
            }
        }
    }

    private void deleteDownstream(ISyncEntity entity){
        if(hasLocalFiles(entity)){

            for(File file : entity.getLocalFiles()){
                if(!file.delete())
                    Log.e(getName(), "Unable to delete: " + file);
            }
        }
    }

    private void updateUpstream(ISyncEntity srcEntity, ISyncEntity destEntity){
        /*
        if(destEntity != null && hasLocalFiles(destEntity)){
            RemoteFile remoteDir = getRemoteDirectory(destEntity);

            for(File file : destEntity.getLocalFiles()){
                String filename = createRemoteFileNameForLocalFile(destEntity, file);
                remoteDir.get(filename).delete();
            }
        }
        */

        if(hasLocalFiles(srcEntity)){
            RemoteFile remoteDir = getRemoteDirectory(srcEntity);

            for(File file : srcEntity.getLocalFiles()){
                LocalFile localFile = new LocalFile(file);
                localFile.setName(createRemoteFileNameForLocalFile(srcEntity, file));

                RemoteFile remoteFile = remoteDir.get(localFile.getName());
                if(remoteFile != null)
                    remoteFile.upload(localFile);
                else
                    remoteFile = remoteDir.create(localFile);

                if(remoteFile == null) {
                    Log.e(getName(), "Unable to create RemoteFile: " + localFile.getName());
                }
            }
        }
    }

    private void updateDownstream(ISyncEntity srcEntity, ISyncEntity destEntity){
        if(destEntity != null && hasLocalFiles(destEntity)){
            for(File file : destEntity.getLocalFiles()){
                if(!file.delete()){
                    Log.w(getName(), "Unable to delete " + file);
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

}
