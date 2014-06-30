package com.bingzer.android.cloudy.providers;

import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.SQLiteSyncManager;
import com.bingzer.android.cloudy.UsingExternalDriveTestCase;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.cloudy.contracts.ISyncProvider;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.RemoteFile;
import com.example.Person;

import java.io.File;

public class RemoteAndLocalProviderTest extends UsingExternalDriveTestCase {
    long syncTimestamp;
    ISyncProvider provider;
    File image1, image2, image3, image4, image5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        syncTimestamp = Timespan.now();

        manager = new SQLiteSyncManager(getContext(), Environment.getLocalEnvironment(), remoteRoot, remoteDbFile);
        provider = SyncProviderFactory.getSyncProvider(manager, ISyncManager.SYNC_DUMP_TO_REMOTE);

        // we need to extract all images from assets to files dir
        File imageDir = new File(getContext().getFilesDir(), "images");
        Path.safeCreateDir(imageDir);

        image1 = new File(imageDir, "01.png");
        if(!image1.exists())
            Path.copyFile(getContext().getAssets().open("01.png"), image1);
        image2 = new File(imageDir, "02.png");
        if(!image2.exists())
            Path.copyFile(getContext().getAssets().open("02.png"), image2);
        image3 = new File(imageDir, "03.png");
        if(!image3.exists())
            Path.copyFile(getContext().getAssets().open("03.png"), image3);
        image4 = new File(imageDir, "04.png");
        if(!image4.exists())
            Path.copyFile(getContext().getAssets().open("04.png"), image4);
        image5 = new File(imageDir, "05.png");
        if(!image5.exists())
            Path.copyFile(getContext().getAssets().open("05.png"), image5);
    }

    @Override
    public String getLocalDatabaseName() {
        return "RemoteDumpProviderTest";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // delete databases
        getContext().deleteDatabase("RemoteDump");
        getContext().deleteDatabase("RemoteDump-Remote");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void test_sync_dumpRemote() throws Exception {
        if(remoteRoot.get("Person") != null){
            for(RemoteFile child : remoteRoot.get("Person").list()){
                child.delete();
            }
        }

        Environment.getLocalEnvironment().getDatabase().get("EntityHistory").delete();
        Environment.getLocalEnvironment().getDatabase().get("Person").delete();

        new Person(Environment.getLocalEnvironment(), "Person1", 1, image1.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person2", 2, image2.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person3", 3, image3.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person4", 4, image4.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person5", 5, image5.getAbsolutePath()).save();

        manager = new SQLiteSyncManager(getContext(), Environment.getLocalEnvironment(), remoteRoot, remoteDbFile);
        manager.syncDatabase(ISyncManager.SYNC_DUMP_TO_REMOTE);

        IEnvironment local = manager.getLocalEnvironment();
        IEnvironment remote = manager.getRemoteEnvironment();

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        int counter = 0;
        for(RemoteFile child : remoteRoot.get("Person").list()){
            counter++;
            child.delete();
        }
        assertEquals(5, counter);

        manager.close();
    }


    public void test_sync_dumpLocal() throws Exception {

        Environment.getLocalEnvironment().getDatabase().get("EntityHistory").delete();
        Environment.getLocalEnvironment().getDatabase().get("Person").delete();
        new Person(Environment.getLocalEnvironment(), "Person1", 1, image1.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person2", 2, image2.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person3", 3, image3.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person4", 4, image4.getAbsolutePath()).save();
        new Person(Environment.getLocalEnvironment(), "Person5", 5, image5.getAbsolutePath()).save();

        manager = new SQLiteSyncManager(getContext(), Environment.getLocalEnvironment(), remoteRoot, remoteDbFile);
        manager.syncDatabase(ISyncManager.SYNC_DUMP_TO_REMOTE);

        IEnvironment local = manager.getLocalEnvironment();
        IEnvironment remote = manager.getRemoteEnvironment();

        ////////////////////////////////////////////////////////////////////////////////////////////

        local.getDatabase().get("EntityHistory").delete();
        local.getDatabase().get("Person").delete();
        assertTrue(image1.delete());
        assertTrue(image2.delete());
        assertTrue(image3.delete());
        assertTrue(image4.delete());
        assertTrue(image5.delete());


        assertEquals(0, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        manager.syncDatabase(ISyncManager.SYNC_DUMP_TO_LOCAL);

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        assertTrue(image1.exists());
        assertTrue(image2.exists());
        assertTrue(image3.exists());
        assertTrue(image4.exists());
        assertTrue(image5.exists());

        manager.close();
    }
}
