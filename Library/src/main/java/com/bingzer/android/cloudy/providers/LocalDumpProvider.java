package com.bingzer.android.cloudy.providers;

import com.bingzer.android.Path;
import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.IEnvironment;

import java.io.File;
import java.io.IOException;

class LocalDumpProvider extends AbsSyncProvider{

    protected LocalDumpProvider(ISyncManager manager, IEnvironment remote) {
        super(manager, remote);
    }

    @Override
    public String getName() {
        return "LocalDumpProvider";
    }

    @Override
    protected void doSync() throws SyncException {
        try {
            File remoteDbFile = new File(local.getDatabase().getPath());
            Path.copyFile(new File(this.remote.getDatabase().getPath()), remoteDbFile);
            local.getDatabase().open(this.remote.getDatabase().getVersion(),
                    remoteDbFile.getAbsolutePath(),
                    new SQLiteSyncBuilder.Copy((SQLiteSyncBuilder) this.remote.getDatabase().getBuilder()));
        }
        catch (IOException e){
            throw new SyncException(e);
        }
    }
}
