package com.example;

import android.content.Context;

import com.bingzer.android.cloudy.SyncEntity;
import com.bingzer.android.cloudy.SyncManager;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.OrmBuilder;
import com.bingzer.android.dbv.contracts.IEntityFactory;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.gdrive.GoogleDrive;

import java.io.File;

@SuppressWarnings("ALL")
class TestUsage {

    void usageInit(Context context){

        IDatabase db = DbQuery.getDatabase("");
        db.open(1, new OrmBuilder() {
            @Override
            public IEntityFactory getEntityFactory() {
                return null;
            }

            @Override
            public Context getContext() {
                return null;
            }

            @Override
            public void onModelCreate(IDatabase iDatabase, IDatabase.Modeling modeling) {

            }
        });

    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    void usageBeforeSyncing(Context context) {

        Credential credential = new Credential(context);
        StorageProvider storageProvider = new GoogleDrive();
        storageProvider.authenticate(credential);

        RemoteFile root = storageProvider.get("MyAppRoot");
        RemoteFile dbRemoteFile = root.get("MyDb");

        SyncManager manager = new SyncManager(context, root);
        manager.syncDatabase(Environment.getLocalEnvironment(), dbRemoteFile);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    private class ClassOne extends SyncEntity {

        @Override
        public String getTableName() {
            return null;
        }
    }

    private class ClassTwo extends SyncEntity {

        private String path;

        @Override
        public String getTableName() {
            return null;
        }

        @Override
        public File[] getLocalFiles() {
            return new File[]{ new File(path) };
        }
    }
}
