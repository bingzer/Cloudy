package com.bingzer.android.cloudy.providers;

import android.util.Log;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ISyncDirectoryProvider;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;
import java.util.List;

class DirectorySyncProvider extends AbsSyncProvider implements ISyncDirectoryProvider {
    private File dir;
    private RemoteFile remoteDir;

    protected DirectorySyncProvider(ISyncManager manager, IEnvironment remote) {
        super(manager, remote);
    }

    @Override
    public String getName() {
        return "DirectorySyncProvider";
    }

    @Override
    public void sync(File dir, RemoteFile remoteDir) throws SyncException {
        this.dir = dir;
        this.remoteDir = remoteDir;
        sync();
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    protected void doSync() throws SyncException {
        final File[] files = dir.listFiles();
        final List<RemoteFile> remoteFiles = remoteDir.list();

        int counter = 0;
        Log.i(getName(), "Uploading files to remote");
        for (File file : files){
            boolean remoteMissing = true;
            for(RemoteFile remoteFile : remoteFiles){
                if(remoteFile.getName().equals(file.getName())){
                    remoteMissing = false;
                    break;
                }
            }

            if(remoteMissing){
                // upload to remote
                remoteDir.create(new LocalFile(file));
                Log.i(getName(), "# " + file.getName());
                ++counter;
            }
        }
        Log.i(getName(), "# Uploaded: " + counter);

        counter = 0;
        Log.i(getName(), "Downloading files to local");
        for (RemoteFile remoteFile : remoteFiles){
            boolean localMissing = true;
            for (File file : files){
                if(file.getName().equals(remoteFile.getName())){
                    localMissing = false;
                    break;
                }
            }

            if(localMissing){
                File file = new File(dir, remoteFile.getName());
                remoteFile.download(new LocalFile(file));
                Log.i(getName(), "# " + file.getName());
                ++counter;
            }
        }
        Log.i(getName(), "# Downloaded: " + counter);
    }
}
