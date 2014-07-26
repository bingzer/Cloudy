package com.bingzer.android.cloudy;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.cloudy.contracts.IDeleteHistory;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.ISyncDirectoryProvider;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.cloudy.providers.SyncProviderFactory;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;

/**
 * The default SQLiteSyncManager
 */
public class SQLiteSyncManager implements ISyncManager {

    private static final String TAG = "SQLiteSyncManager";

    private final Context context;
    private final IEnvironment local;

    private final RemoteFile root;
    private final RemoteFile remoteDbFile;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, IEnvironment local, RemoteFile root, RemoteFile remoteDbFile){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.remoteDbFile = remoteDbFile;
        this.context = context.getApplicationContext();
        this.local = local;

        LocalConfiguration.seedConfigs(local);
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public IEnvironment getLocalEnvironment() {
        return local;
    }

    @Override
    public IDeleteHistory createDeleteHistory(IEnvironment environment) {
        return new DeleteHistory(environment);
    }

    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public RemoteFile getRemoteDbFile() {
        return remoteDbFile;
    }

    @Override
    public ILocalConfiguration getConfig(String name) {
        return LocalConfiguration.getConfig(local, name);
    }

    /**
     * Sync database
     */
    @Override
    public void syncDatabase(int syncType) throws SyncException {
        IEnvironment remote = createRemoteEnvironment();
        ISyncProvider syncProvider = SyncProviderFactory.getSyncProvider(this, remote, syncType);
        syncProvider.sync();
        syncProvider.close();
    }

    @Override
    public void syncFiles(File dir, RemoteFile remoteDir) throws SyncException {
        ISyncDirectoryProvider syncProvider = (ISyncDirectoryProvider) SyncProviderFactory.getSyncProvider(this, null, SYNC_FILES);
        syncProvider.sync(dir, remoteDir);
        syncProvider.close();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected IEnvironment createRemoteEnvironment(){
        Log.i(TAG, "Creating remote environment in the cache");
        final IDatabase localDb = local.getDatabase();

        Log.i(TAG, "Downloading remote db file");
        final LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));
        if(!remoteDbFile.download(dbLocalFile))
            throw new RuntimeException("Failed to download");

        Log.i(TAG, "Opening remote for future use");
        final IDatabase db = DbQuery.getDatabase(localDb.getName() + "-remote");
        db.open(localDb.getVersion(),
                dbLocalFile.getFile().getAbsolutePath(),
                new SQLiteSyncBuilder.Copy((SQLiteSyncBuilder) localDb.getBuilder()));

        return new Environment(db);
    }

}
