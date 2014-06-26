package com.bingzer.android.cloudy;

import android.content.Context;
import android.util.Log;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
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
import java.io.IOException;
import java.util.List;

/**
 * The default SQLiteSyncManager
 */
public class SQLiteSyncManager implements ISyncManager {

    private static final String TAG = "SQLiteSyncManager";

    private long lockTimeout;

    private final Context context;
    private final IEnvironment local;

    private final RemoteFile root;
    private final RemoteFile remoteDbFile;
    private RemoteFile revisionFile;   // 1321346465.revision
    private RemoteFile lockFile;       // 1231465466.lock
    private List<RemoteFile> rootChildren;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, IEnvironment local, RemoteFile root, RemoteFile remoteDbFile){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.remoteDbFile = remoteDbFile;
        this.context = context.getApplicationContext();
        this.local = local;
        LocalConfiguration.seedConfigs(local);

        lockTimeout = LocalConfiguration.getConfig(local, LocalConfiguration.SETTING_LOCK_TIMEOUT).getValueAsLong();
        rootChildren = getRootChildren(true);
    }

    @Override
    public IEnvironment getLocalEnvironment() {
        return local;
    }

    @Override
    public IEnvironment getRemoteEnvironment() {
        Log.i(TAG, "- Creating remote environment in the cache");
        IEnvironment environment = null;
        try{
            IDatabase localDb = local.getDatabase();
            final LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));

            Log.i(TAG, "Downloading remote db file");
            remoteDbFile.download(dbLocalFile);

            Log.i(TAG, "Opening remote for future use");
            final IDatabase db = DbQuery.getDatabase(localDb.getName() + "-remote");
            db.open(localDb.getVersion(), dbLocalFile.getFile().getAbsolutePath(), localDb.getBuilder());

            return (environment = new Environment(db));
        }
        finally {
            Log.i(TAG, "- Creating remote environment = " + (environment != null));
        }
    }

    @Override
    public IEntityHistory createEntityHistory(IEnvironment environment) {
        return new EntityHistory(environment);
    }

    /**
     * Returns the Root
     */
    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public RemoteFile getRemoteDbFile() {
        return remoteDbFile;
    }

    /**
     * Sync database
     */
    @Override
    public void syncDatabase(int syncType) throws SyncException {
        Log.i(TAG, "----- Starting syncDatabase(). SyncType = " + syncType);
        try{
            long revision = LocalConfiguration.getConfig(local, LocalConfiguration.SETTING_REVISION).getValueAsLong();

            doSync(syncType, revision);
        }
        catch (Exception e){
            Log.e(TAG, "----- " + e.getMessage());
            if(e instanceof SyncException) throw (SyncException) e;
            else throw new SyncException(e);
        }
        finally {
            Log.i(TAG, "----- End of syncDatabase()");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    RemoteFile ensureRevisionExists(){
        revisionFile = null;
        for(RemoteFile child : getRootChildren(false)){
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

                LocalFile localFile = new LocalFile(f);
                localFile.setName("-1.revision");
                revisionFile = root.create(localFile);
            }
            catch (IOException e){
                throw new SyncException(e);
            }
        }

        return revisionFile;
    }

    RemoteFile ensureLockExists(){
        lockFile = null;
        for(RemoteFile child : getRootChildren(false)){
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

                LocalFile localFile = new LocalFile(f);
                localFile.setName(now + ".revision");
                lockFile = root.create(localFile);
            }
            catch (IOException e){
                throw new SyncException(e);
            }
        }

        return lockFile;
    }

    /**
     * Acquire lock on the remote side. Returns true if success.
     * Otherwise, there's another syncing progress by other client
     */
    boolean acquireLock(){
        Log.i(TAG, "- Acquiring lock on the remote side");
        boolean lockAcquired = false;
        try {
            rootChildren = getRootChildren(true);

            lockFile = null;
            for (RemoteFile child : rootChildren) {
                if (child.getName().endsWith(".lock")) {
                    lockFile = child;
                    break;
                }
            }

            if (lockFile != null) {
                long timestamp = Parser.parseLong(Path.stripExtension(lockFile.getName()), -1);
                // see if it's expired
                lockAcquired = Math.abs(Timespan.now() - timestamp) > lockTimeout;
            } else {
                ensureLockExists();
                lockAcquired = lockFile != null;
            }

            return lockAcquired;
        }
        finally {
            Log.i(TAG, "- Acquiring Lock = " + lockAcquired);
        }
    }

    /**
     * Compare local and remote revision. If the two doesn't match we should sync (returns true).
     * Otherwise, returns false.
     */
    boolean shouldSync(long localRevision){
        Log.i(TAG, "- Checking remote revision. localRevision = " + localRevision);
        boolean shouldSync = true;
        try{
            revisionFile = ensureRevisionExists();
            if(revisionFile != null){
                // check the revision
                long remoteRevision = Parser.parseLong(Path.stripExtension(revisionFile.getName()), -1);
                Log.d(TAG, "localRevision  = " + localRevision);
                Log.d(TAG, "remoteRevision = " + remoteRevision);
                shouldSync = remoteRevision != localRevision;
            }

            return shouldSync;
        }
        finally {
            Log.i(TAG, "- Should sync? " + shouldSync);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private List<RemoteFile> getRootChildren(boolean refresh){
        if(refresh) rootChildren = null;

        if(rootChildren == null)
            return root.list();
        return rootChildren;
    }

    private void doSync(int syncType, long revision){
        Log.d(TAG, "doSync()");
        Log.d(TAG, "dbRemoteFile = " + remoteDbFile.getName());
        Log.d(TAG, "revision = " + revision);

        ISyncProvider syncProvider = null;
        try{
            if(!shouldSync(revision)) throw new SyncException.NoChanges();
            if(!acquireLock()) throw new SyncException.OtherIsSyncing();

            ensureRevisionExists();

            syncProvider = SyncProviderFactory.getSyncProvider(this, syncType);
            long newTimestamp = syncProvider.sync(revision);

            // Client REVISION (Local only)
            Log.d(TAG, "Updating LocalConfiguration's Revision to: " + newTimestamp);
            ILocalConfiguration config = LocalConfiguration.getConfig(local, LocalConfiguration.SETTING_CLIENTID);
            config.setValue(newTimestamp);
            config.save();

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

}
