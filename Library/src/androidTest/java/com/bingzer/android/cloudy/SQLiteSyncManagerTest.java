package com.bingzer.android.cloudy;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.example.Person;
import com.example.TestDbBuilder;

import java.io.File;

public class SQLiteSyncManagerTest extends UsingExternalDriveTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = new SQLiteSyncManager(getContext(), Environment.getLocalEnvironment(), remoteRoot);
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_getRoot(){
        assertNotNull(manager.getRoot());
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_sync(){
        deleteLock();

        // TODO: check before and after sync
        IEnvironment local = Environment.getLocalEnvironment();
        local.getDatabase().open(1, new TestDbBuilder(getContext()));

        new Person(local, "Person1", 1).save();
        new Person(local, "Person2", 2).save();
        new Person(local, "Person3", 3).save();
        new Person(local, "Person4", 4).save();
        new Person(local, "Person5", 5).save();

        //assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        //assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        //assertEquals(5, local.getDatabase().get("Person").count());
        //assertEquals(0, remote.getDatabase().get("Person").count());

        manager.syncDatabase(remoteDbFile);
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_acquireLock() throws Exception{
        deleteLock();

        String name = Timespan.now() + ".lock";
        File f = new File(getContext().getFilesDir(), name);
        f.createNewFile();
        RemoteFile lockFile = storageProvider.create(remoteRoot, name, new LocalFile(f));

        assertFalse(manager.acquireLock());

        lockFile.delete();

        assertTrue(manager.acquireLock());
        deleteLock();
    }

    public void test_shouldNotSync() throws Exception {
        manager.syncDatabase(remoteDbFile);

        long existingTimestamp = 0;
        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".revision"))
                existingTimestamp = Parser.parseLong(Path.stripExtension(child.getName()), -1);
        }

        assertTrue(existingTimestamp > 0);

        // we should not sync
        try{
            IEnvironment environment = Environment.getLocalEnvironment();
            environment.getDatabase().open(1, new TestDbBuilder(getContext()));
            ILocalConfiguration config = LocalConfiguration.getConfig(environment, LocalConfiguration.SETTING_REVISION);
            config.setValue(existingTimestamp);
            assertTrue(config.save());

            manager.syncDatabase(remoteDbFile);
            fail("Should throw syncexception");
        }
        catch (SyncException e){
            assertEquals("Everything is up-to-date", e.getMessage());
            assertTrue("Good", true);

        }

        deleteRevision();
    }

    private void deleteRevision(){
        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".revision"))
                child.delete();
        }
    }

    private void deleteLock(){
        for(RemoteFile child : remoteRoot.list()){
            if(child.getName().endsWith(".lock"))
                child.delete();
        }
    }

}
