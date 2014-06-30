package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.Path;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.local.ExternalDrive;
import com.example.TestDbBuilder;

import java.io.File;

public abstract class UsingExternalDriveTestCase extends AndroidTestCase{

    protected SQLiteSyncManager manager;
    protected StorageProvider storageProvider;
    protected RemoteFile remoteRoot;
    protected RemoteFile remoteDbFile;
    protected IDatabase localDb;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        localDb = DbQuery.getDatabase(getLocalDatabaseName());
        localDb.open(1, new TestDbBuilder(getContext()));

        final String REMOTE_ROOT_NAME = "remoteRoot";

        File rootFile = new File(android.os.Environment.getExternalStorageDirectory(), "cloudy-remote-test");
        Path.deleteTree(new File(rootFile, REMOTE_ROOT_NAME), true);

        storageProvider = new ExternalDrive();
        storageProvider.authenticate(new Credential(getContext(), rootFile.getAbsolutePath()));
        remoteRoot = storageProvider.create(REMOTE_ROOT_NAME);
        remoteDbFile = storageProvider.create(remoteRoot, new LocalFile(new File(localDb.getPath())));

        assertNotNull(remoteRoot);
        assertNotNull(remoteDbFile);
    }

    public abstract String getLocalDatabaseName();
}
