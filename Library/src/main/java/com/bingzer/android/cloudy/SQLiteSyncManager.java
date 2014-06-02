package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IClientRevision;
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

    private RemoteFile root;
    private List<RemoteFile> childrenOfRoot;
    private Context context;
    private ClientRevision localRevision;

    private RemoteFile revisionFile;   // 1321346465.revision
    private RemoteFile lockFile;       // 1231465466.lock

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, IEnvironment local, RemoteFile root){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.context = context.getApplicationContext();
        this.localRevision = new ClientRevision(context, local);
    }

    @Override
    public IClientRevision getClientRevision() {
        return localRevision;
    }

    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public void syncDatabase(RemoteFile dbRemoteFile) {
        childrenOfRoot = root.list();
        ISyncProvider syncProvider = null;
        try{
            if(!shouldSync(localRevision.getRevision())) throw new SyncException("Everything is up-to-date");
            if(!acquireLock()) throw new SyncException("Another client is syncing");
            ensureRevisionExists(childrenOfRoot);

            IEnvironment remote = createRemoteEnvironment(dbRemoteFile);

            syncProvider = new SyncProvider(this, localRevision.getEnvironment(), remote);
            long newTimestamp = syncProvider.sync(localRevision.getRevision());

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
        IEnvironment local = localRevision.getEnvironment();
        IDatabase localDb = local.getDatabase();
        LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));
        remoteDbFile.download(dbLocalFile);

        IDatabase db = DbQuery.getDatabase(localDb.getName() + "-remote");
        db.open(localDb.getVersion(), dbLocalFile.getFile().getAbsolutePath(), localDb.getBuilder());

        return new Environment(db);
    }

}
