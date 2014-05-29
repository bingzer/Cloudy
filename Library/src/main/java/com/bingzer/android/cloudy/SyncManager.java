package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.cloudy.contracts.IEntityFactory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.cloudy.entities.Environment;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.SQLiteBuilder;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;

import java.io.File;

public class SyncManager implements ISyncManager {

    private StorageProvider storageProvider;
    private RemoteFile root;
    private DatabaseMapping dbMapping;
    private Context context;
    private ISyncProvider syncProvider;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SyncManager(Context context, StorageProvider storageProvider){
        this.context = context.getApplicationContext();
        this.storageProvider = storageProvider;
    }

    @Override
    public void syncRoot(RemoteFile root) {
        if(!root.isDirectory()) throw new SyncException("root must be a directory");
        this.root = root;
    }

    @Override
    public void syncDatabase(IDatabase local, RemoteFile dbRemoteFile, IEntityFactory factory) {
        dbMapping = new DatabaseMapping(local, dbRemoteFile, factory);
    }

    @Override
    public void sync() {
        IEnvironment local = Environment.getLocalEnvironment();
        IEnvironment remote = new Environment(getRemoteDb(), local.getEntityFactory());
        syncProvider = new SyncProvider(local, remote);
        syncProvider.sync(1);
    }

    private IDatabase getRemoteDb(){
        IDatabase localDb = Environment.getLocalEnvironment().getDatabase();

        LocalFile dbLocalFile = downloadRemoteDbToLocal();
        IDatabase db = DbQuery.getDatabase(localDb.getName());
        db.open(localDb.getVersion(), dbLocalFile.getFile().getAbsolutePath(), new SQLiteBuilder.WithoutModeling(context));

        return db;
    }

    private LocalFile downloadRemoteDbToLocal(){
        LocalFile localFile = new LocalFile(new File(context.getCacheDir(), dbMapping.dbRemoteFile.getName()));
        dbMapping.dbRemoteFile.download(localFile);
        return localFile;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    private class DatabaseMapping {
        private IDatabase local;
        private RemoteFile dbRemoteFile;
        private IEntityFactory factory;
        private DatabaseMapping(IDatabase local, RemoteFile dbRemoteFile, IEntityFactory factory){
            this.local = local;
            this.dbRemoteFile = dbRemoteFile;
            this.factory = factory;
        }
    }

}
