package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.Credential;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.local.ExternalDrive;
import com.example.Person;
import com.example.TestDbBuilder;

import java.io.File;

public class SQLiteSyncManagerTest extends AndroidTestCase {

    private SQLiteSyncManager manager;
    private File clientFile;
    private StorageProvider storageProvider;
    private RemoteFile remoteRoot;
    private RemoteFile remoteDbFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IDatabase dbSample = DbQuery.getDatabase("SampleDb");
        dbSample.open(1, new TestDbBuilder(getContext()));

        File rootFile = new File(android.os.Environment.getExternalStorageDirectory(), "cloudy-remote-test");
        storageProvider = new ExternalDrive();
        storageProvider.authenticate(new Credential(getContext(), rootFile.getAbsolutePath()));
        remoteRoot = storageProvider.create("remoteRoot");
        remoteDbFile = storageProvider.create(remoteRoot, "remoteDb", new LocalFile(new File(dbSample.getPath())));

        manager = new SQLiteSyncManager(getContext(), remoteRoot);
        clientFile = new File(getContext().getFilesDir(), "Cloudy.Client");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // TODO: delete local database so we're fresh
        //remote.getDatabase().close();
        //local.getDatabase().close();
        // delete databases
        //getContext().deleteDatabase("SyncProviderTest-Local");
        //getContext().deleteDatabase("SyncProviderTest-Remote");
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_getRoot(){
        assertNotNull(manager.getRoot());
    }

    public void test_getClientId(){
        assertTrue(manager.getClientId() != -1);
        assertTrue(clientFile.exists());
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_sync(){
        // TODO: check before and after sync
        IEnvironment local = Environment.getLocalEnvironment();
        new Person(local, "Person1", 1).save();
        new Person(local, "Person2", 2).save();
        new Person(local, "Person3", 3).save();
        new Person(local, "Person4", 4).save();
        new Person(local, "Person5", 5).save();

        //assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        //assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        //assertEquals(5, local.getDatabase().get("Person").count());
        //assertEquals(0, remote.getDatabase().get("Person").count());

        manager.syncDatabase(local, remoteDbFile);
    }

}
