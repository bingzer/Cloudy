package com.example;

import android.content.Context;

import com.bingzer.android.cloudy.SyncManager;
import com.bingzer.android.cloudy.entities.BaseEntity;
import com.bingzer.android.cloudy.entities.Environment;
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

        Credential credential = new Credential(context);
        StorageProvider storageProvider = new GoogleDrive();
        storageProvider.authenticate(credential);

        RemoteFile root = storageProvider.get("MyAppRoot");
        RemoteFile dbRemoteFile = root.get("MyDb");

        SyncManager manager = new SyncManager(context, root);
        manager.syncDatabase(Environment.getLocalEnvironment(), dbRemoteFile);
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
        public File[] getLocalFiles() {
            return new File[]{ new File(path) };
        }
    }
}
