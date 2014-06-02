package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Randomite;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.bingzer.android.Stringify.isNullOrEmpty;

public class SQLiteSyncManager implements ISyncManager {

    private RemoteFile root;
    private List<RemoteFile> childrenOfRoot;
    private Context context;
    private ClientInfo clientInfo;

    private RemoteFile revisionFile;   // 1321346465.revision
    private RemoteFile lockFile;       // 1231465466.lock

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, RemoteFile root){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.context = context.getApplicationContext();
        this.clientInfo = new ClientInfo(context);
    }

    @Override
    public long getClientId() {
        return clientInfo.getClientId();
    }

    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public void syncDatabase(IEnvironment local, RemoteFile dbRemoteFile) {
        clientInfo.setEnvironment(local);
        childrenOfRoot = root.list();
        ISyncProvider syncProvider = null;
        try{
            if(!shouldSync(clientInfo.getClientSyncInfo().getRevision())) throw new SyncException("Everything is up-to-date");
            if(!acquireLock()) throw new SyncException("Another client is syncing");
            ensureRevisionExists(childrenOfRoot);

            IEnvironment remote = clientInfo.createRemoteEnvironment(dbRemoteFile);

            syncProvider = new SyncProvider(this, local, remote);
            long newTimestamp = syncProvider.sync(clientInfo.getClientSyncInfo().getRevision());

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

    IClientSyncInfo getClientSyncInfo(){
        return clientInfo.getClientSyncInfo();
    }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected static class ClientInfo {

        private static long INVALID_CLIENT_ID = -1;
        private long clientId = INVALID_CLIENT_ID;
        private final File clientFile;
        private IEnvironment environment;
        private Context context;

        public ClientInfo(Context context){
            this.context = context;
            clientFile = new File(context.getFilesDir(), "Cloudy.Client");
        }

        void setEnvironment(IEnvironment environment){
            this.environment = environment;
        }

        long getClientId() {
            if(clientId == INVALID_CLIENT_ID){
                try {
                    clientId = generateUniqueId();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return clientId;
        }

        IClientSyncInfo getClientSyncInfo(){
            IClientSyncInfo clientSyncInfo = ClientSyncInfo.getClient(environment, getClientId());
            clientSyncInfo.setClientId(getClientId());
            clientSyncInfo.save();
            return clientSyncInfo;
        }

        private long generateUniqueId() throws IOException{
            if(clientFile.exists()){
                // read from file
                return readClientFile();
            }
            else{
                return writeClientFile();
            }
        }

        private long readClientFile() throws IOException{
            BufferedReader br = new BufferedReader(new FileReader(clientFile));
            String input = br.readLine();
            Path.safeClose(br);

            long uniqueId = INVALID_CLIENT_ID;
            if(!isNullOrEmpty(input)){
                uniqueId = Parser.parseLong(input, INVALID_CLIENT_ID);
                if(uniqueId == INVALID_CLIENT_ID)
                    throw new IOException("Not a valid ClientId: " + input);
            }
            return uniqueId;
        }

        private long writeClientFile() throws IOException {
            Long uniqueId = Randomite.uniqueId();

            FileWriter fw = new FileWriter(clientFile);
            fw.write(uniqueId.toString());
            fw.close();

            return readClientFile();
        }

        private IEnvironment createRemoteEnvironment(RemoteFile remoteDbFile){
            LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));
            remoteDbFile.download(dbLocalFile);

            IDatabase db = DbQuery.getDatabase(environment.getDatabase().getName() + "-remote");
            db.open(environment.getDatabase().getVersion(), dbLocalFile.getFile().getAbsolutePath(), environment.getDatabase().getBuilder());

            return new Environment(db);
        }
    }
}
