package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.local.ExternalDrive;
import com.example.TestDbBuilder;

import org.mockito.cglib.core.Local;

import java.io.File;

public class UsingExternalDriveTestCase extends AndroidTestCase{

    protected SQLiteSyncManager manager;
    protected StorageProvider storageProvider;
    protected RemoteFile remoteRoot;
    protected RemoteFile remoteDbFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IDatabase dbSample = DbQuery.getDatabase("SampleDb");
        dbSample.open(1, new TestDbBuilder(getContext()));

        File rootFile = new File(android.os.Environment.getExternalStorageDirectory(), "cloudy-remote-test");
        storageProvider = new ExternalDrive();
        storageProvider.authenticate(new Credential(getContext(), rootFile.getAbsolutePath()));
        remoteRoot = storageProvider.create("remoteRoot");
        remoteDbFile = storageProvider.create(remoteRoot, new LocalFile(new File(dbSample.getPath())));
    }
}
