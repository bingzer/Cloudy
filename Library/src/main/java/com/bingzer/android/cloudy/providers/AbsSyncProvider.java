package com.bingzer.android.cloudy.providers;

import android.util.Log;

import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.IEnvironment;

import java.io.File;

abstract class AbsSyncProvider implements ISyncProvider{
    protected final IEnvironment local;
    protected final ISyncManager manager;
    protected final IEnvironment remote;

    protected AbsSyncProvider(ISyncManager manager, IEnvironment remote){
        this.manager = manager;
        this.local = manager.getLocalEnvironment();
        this.remote = remote;
    }

    @Override
    public final void sync() throws SyncException {
        Log.i(getName(), "SyncProvider = " + getName());
        try{
            Log.i(getName(), "Starting sync process");
            doSync();
        }
        catch (Throwable e){
            Log.e(getName(), e.getMessage(), e);
        }
        finally {
            Log.i(getName(), "End of sync process");
        }
    }

    @Override
    public void close() {
        if(remote != null){
            File db = new File(remote.getDatabase().getPath());
            // close
            remote.getDatabase().close();
            if(!db.delete())
                Log.e(getName(), "Cleanup() - failed to delete remote db");
        }

        // update last sync config
        updateTimestamp();

        Log.i(getName(), getName() + " is now Closed!");
    }

    @Override
    public abstract String getName();

    //////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void doSync() throws SyncException;

    //////////////////////////////////////////////////////////////////////////////////////////////

    private void updateTimestamp(){
        long now = Timespan.now() - Timespan.DAYS_1;
        Log.i(getName(), "Updating LocalConfig to " + now);
        manager.getConfig(ILocalConfiguration.LastSync).setValue(now).save();
    }

}
