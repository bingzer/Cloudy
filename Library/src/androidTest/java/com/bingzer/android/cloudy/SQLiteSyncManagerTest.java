package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
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

    /////////////////////////////////////////////////////////////////////////////////

    public void test_acquireLock() throws Exception{
        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".lock"))
                child.delete();
        }

        String name = Timespan.now() + ".lock";
        File f = new File(getContext().getFilesDir(), name);
        f.createNewFile();
        RemoteFile lockFile = storageProvider.create(remoteRoot, name, new LocalFile(f));

        assertFalse(manager.acquireLock());

        lockFile.delete();

        assertTrue(manager.acquireLock());

        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".lock"))
                child.delete();
        }
    }

    public void test_shouldNotSync() throws Exception {
        IEnvironment local = Environment.getLocalEnvironment();
        manager.syncDatabase(local, remoteDbFile);

        long existingTimestamp = 0;
        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".revision"))
                existingTimestamp = Parser.parseLong(Path.stripExtension(child.getName()), -1);
        }

        assertTrue(existingTimestamp > 0);

        // we should not sync
        try{
            Environment.getLocalEnvironment().getDatabase().open(1, new TestDbBuilder(getContext()));
            IClientSyncInfo clientSyncInfo = manager.getClientSyncInfo();
            clientSyncInfo.setRevision(existingTimestamp);
            assertTrue(clientSyncInfo.save());

            manager.syncDatabase(local, remoteDbFile);
            fail("Should throw syncexception");
        }
        catch (SyncException e){
            assertEquals("Everything is up-to-date", e.getMessage());
            assertTrue("Good", true);

        }

        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".revision"))
                child.delete();
        }
    }

}
