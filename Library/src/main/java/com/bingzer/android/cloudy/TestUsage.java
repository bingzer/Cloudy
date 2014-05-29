package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.cloudy.contracts.IEntityFactory;
import com.bingzer.android.cloudy.entities.BaseEntity;
import com.bingzer.android.cloudy.entities.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.gdrive.GoogleDrive;

import java.io.File;

@SuppressWarnings("ALL")
class TestUsage {

    void usageInit(Context context){

    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    void usageBeforeSyncing(Context context) {
        IDatabase localDb = Environment.getLocalEnvironment().getDatabase();

        Credential credential = new Credential(context);
        StorageProvider storageProvider = new GoogleDrive();
        storageProvider.authenticate(credential);

        RemoteFile root = storageProvider.get("MyAppRoot");
        RemoteFile dbRemoteFile = storageProvider.get(root, "MyDb");

        SyncManager manager = new SyncManager(context, storageProvider);
        manager.syncRoot(root);
        manager.syncDatabase(localDb, dbRemoteFile, new IEntityFactory() {
            public BaseEntity createEntity(String tableName) {
                if(tableName.equals("ClassOne"))
                    return new ClassOne();
                else if(tableName.equals("ClassTwo"))
                    return new ClassTwo();
                return null;
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    private class ClassOne extends BaseEntity {

        @Override
        public String getTableName() {
            return null;
        }
    }

    private class ClassTwo extends BaseEntity {

        private String path;

        @Override
        public String getTableName() {
            return null;
        }

        @Override
        protected File[] getLocalFiles() {
            return new File[]{ new File(path) };
        }
    }
}
