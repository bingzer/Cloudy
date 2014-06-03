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

    public void test_yield_to_otherclients() throws Exception{
        deleteLock();

        String name = Timespan.now() + ".lock";
        File f = new File(getContext().getFilesDir(), name);
        f.createNewFile();
        RemoteFile lockFile = storageProvider.create(remoteRoot, name, new LocalFile(f));

        // -- mocking other client is syncing
        try{
            manager.syncDatabase(remoteDbFile);
            fail("Should throw exception");
        }
        catch (SyncException e){
            assertEquals("Other client is syncing. Must yield.", e.getMessage());
            assertTrue("Good", true);
        }

        // mocking other client is syncing but the lock timeout is expire (default is 30 minutes)
        lockFile.delete();
        name = (Timespan.now() - (Timespan.MINUTES_30 + 1)) + ".lock";
        f = new File(getContext().getFilesDir(), name);
        f.createNewFile();
        lockFile = storageProvider.create(remoteRoot, name, new LocalFile(f));

        try{
            manager.syncDatabase(remoteDbFile);
            assertTrue("Good", true);
        }
        catch (SyncException e){
            fail("Shouldn't throw exception");
        }

        lockFile.delete();

        deleteLock();
    }

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
            assertEquals("No changes detected", e.getMessage());
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
