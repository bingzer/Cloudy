package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Randomite;
import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.SQLiteBuilder;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import static com.bingzer.android.Stringify.isNullOrEmpty;

public class SQLiteSyncManager implements ISyncManager {

    private static long INVALID_CLIENT_ID = -1;

    private RemoteFile root;
    private Context context;
    private long clientId;
    private final File clientFile;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SQLiteSyncManager(Context context, RemoteFile root){
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
        this.context = context.getApplicationContext();
        this.clientFile = new File(context.getFilesDir(), "Cloudy.Client");
    }

    @Override
    public long getClientId() {
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

    @Override
    public RemoteFile getRoot() {
        return root;
    }

    @Override
    public void syncDatabase(IEnvironment local, RemoteFile dbRemoteFile) {
        IClientSyncInfo client = ClientSyncInfo.getClient(local, getClientId());

        IEnvironment remote = createRemoteEnvironment(local, dbRemoteFile);

        ISyncProvider syncProvider = new SyncProvider(this, local, remote);
        syncProvider.sync(client.getLastSync());
    }

    //////////////////////////////////////////////////////////////////////////////////////////

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
        StringBuilder builder = new StringBuilder();
        Path.copy(new FileInputStream(clientFile), builder);
        String input = builder.toString();

        if(!isNullOrEmpty(input)){
            return Parser.parseLong(input, INVALID_CLIENT_ID);
        }

        return INVALID_CLIENT_ID;
    }

    private long writeClientFile() throws IOException {
        long uniqueId = Randomite.uniqueId();

        FileWriter fw = new FileWriter(clientFile);
        fw.write(uniqueId + "");
        fw.flush();
        Path.safeClose(fw);

        return readClientFile();
    }

    private IEnvironment createRemoteEnvironment(IEnvironment local, RemoteFile remoteDbFile){
        LocalFile dbLocalFile = new LocalFile(new File(context.getCacheDir(), remoteDbFile.getName()));
        remoteDbFile.download(dbLocalFile);

        IDatabase db = DbQuery.getDatabase(local.getDatabase().getName());
        db.open(local.getDatabase().getVersion(), dbLocalFile.getFile().getAbsolutePath(), new SQLiteBuilder.WithoutModeling(context));

        return new Environment(db);
    }

}
