package com.bingzer.android.cloudy;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Randomite;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SQLiteSyncManager implements ISyncManager {

    private static final String TAG = "SQLiteSyncManager";
    private RemoteFile root;
    private List<RemoteFile> childrenOfRoot;
    private Context context;
    private IEnvironment local;

    private RemoteFile revisionFile;   // 1321346465.revision
    private RemoteFile lockFile;       // 1231465466.lock

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, IEnvironment local, RemoteFile root){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.context = context.getApplicationContext();
        this.local = local;
        seedConfigs(local);
    }

    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public void syncDatabase(RemoteFile dbRemoteFile) {
        childrenOfRoot = root.list();
        ISyncProvider syncProvider = null;
        long revision = LocalConfiguration.getConfig(local, LocalConfiguration.SETTING_REVISION).getValueAsLong();
        try{
            if(!shouldSync(revision)) throw new SyncException("Everything is up-to-date");
            if(!acquireLock()) throw new SyncException("Another client is syncing");
            ensureRevisionExists(childrenOfRoot);

            IEnvironment remote = createRemoteEnvironment(dbRemoteFile);

            syncProvider = new SyncProvider(this, local, remote);
            long newTimestamp = syncProvider.sync(revision);

            if(!revisionFile.rename(newTimestamp + ".revision"))
                throw new SyncException("Failed to commit new revision");
            if(!lockFile.delete())
                throw new SyncException("Failed to delete lock");
        }
        finally {
            if(syncProvider != null)
                syncProvider.cleanup();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    RemoteFile ensureRevisionExists(List<RemoteFile> childrenOfRoot){
        if(childrenOfRoot == null)
            childrenOfRoot = root.list();

        revisionFile = null;
        for(RemoteFile child : childrenOfRoot){
            if(child.getName().endsWith(".revision")){
                revisionFile = child;
                break;
            }
        }

        if(revisionFile == null){
            try {
                File f = new File(context.getFilesDir(), "-1.revision");
                if (!f.exists() && !f.createNewFile())
                    throw new SyncException("Unable to create file: " + f);
                revisionFile = root.create("-1.revision", new LocalFile(f));
            }
            catch (IOException e){
                throw new SyncException(e);
            }
        }

        return revisionFile;
    }

    RemoteFile ensureLockExists(List<RemoteFile> childrenOfRoot){
        if(childrenOfRoot == null)
            childrenOfRoot = root.list();

        lockFile = null;
        for(RemoteFile child : childrenOfRoot){
            if(child.getName().endsWith(".lock")){
                lockFile = child;
                break;
            }
        }

        long now = Timespan.now();
        if(lockFile == null){
            try {
                File f = new File(context.getFilesDir(), now + ".lock");
                if (!f.exists() && !f.createNewFile())
                    throw new SyncException("Unable to create file: " + f);
                lockFile = root.create(now + ".lock", new LocalFile(f));
            }
            catch (IOException e){
                throw new SyncException(e);
            }
        }

        return lockFile;
    }

    boolean acquireLock(){
        childrenOfRoot = root.list();

        lockFile = null;
        for(RemoteFile child : childrenOfRoot){
            if(child.getName().endsWith(".lock")){
                lockFile = child;
                break;
            }
        }

        if(lockFile != null){
            long timestamp = Parser.parseLong(Path.stripExtension(lockFile.getName()), -1);
            return Math.abs(Timespan.now() - timestamp) > Timespan.MINUTES_30;
        }
        else{
            ensureLockExists(childrenOfRoot);
            return lockFile != null;
        }
    }

    boolean shouldSync(long localRevision){
        revisionFile = ensureRevisionExists(childrenOfRoot);
        if(revisionFile != null){
            // check the revision
            long remoteRevision = Parser.parseLong(Path.stripExtension(revisionFile.getName()), -1);
            return remoteRevision != localRevision;
        }
        return true;
    }

    private IEnvironment createRemoteEnvironment(RemoteFile remoteDbFile){
        IDatabase localDb = local.getDatabase();
        LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));
        remoteDbFile.download(dbLocalFile);

        IDatabase db = DbQuery.getDatabase(localDb.getName() + "-remote");
        db.open(localDb.getVersion(), dbLocalFile.getFile().getAbsolutePath(), localDb.getBuilder());

        return new Environment(db);
    }

    /**
     * Seed all configs if it does not exists
     */
    private void seedConfigs(IEnvironment env){
        ILocalConfiguration config;
        if(!LocalConfiguration.hasConfig(env, LocalConfiguration.SETTING_CLIENTID)){
            config = LocalConfiguration.getConfig(env, LocalConfiguration.SETTING_CLIENTID);
            config.setValue(Randomite.uniqueId());
            config.save();
            Log.i(TAG, "Seeding " + LocalConfiguration.SETTING_CLIENTID + " with value: " + config.getValue());
        }

        if(!LocalConfiguration.hasConfig(env, LocalConfiguration.SETTING_LOCK_TIMEOUT)){
            config = LocalConfiguration.getConfig(env, LocalConfiguration.SETTING_LOCK_TIMEOUT);
            config.setValue(Timespan.MINUTES_30);
            config.save();
            Log.i(TAG, "Seeding " + LocalConfiguration.SETTING_LOCK_TIMEOUT + " with value: " + config.getValue());
        }

        if(!LocalConfiguration.hasConfig(env, LocalConfiguration.SETTING_REVISION)){
            config = LocalConfiguration.getConfig(env, LocalConfiguration.SETTING_REVISION);
            config.setValue(0);
            config.save();
            Log.i(TAG, "Seeding " + LocalConfiguration.SETTING_LOCK_TIMEOUT + " with value: " + config.getValue());
        }
    }

}
