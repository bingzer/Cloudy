package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IClientRevision;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncManager;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.example.Person;
import com.example.TestDbBuilder;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SyncProviderTest extends AndroidTestCase {

    SyncProvider provider;
    IEnvironment remote;
    IEnvironment local;
    long syncTimestamp;
    File image1, image2, image3, image4, image5;

    @Override
    protected void setUp() throws Exception {
        syncTimestamp = Timespan.now();
        IDatabase localDb = DbQuery.getDatabase("SyncProviderTest-Local");
        localDb.open(1, new TestDbBuilder(getContext()));
        IDatabase remoteDb = DbQuery.getDatabase("SyncProviderTest-Remote");
        remoteDb.open(1, new TestDbBuilder(getContext()));

        local = new Environment(localDb);
        remote = new Environment(remoteDb);
        ISyncManager manager = mock(ISyncManager.class);
        when(manager.getClientRevision()).thenReturn(mock(IClientRevision.class));

        provider = new SyncProvider(manager, local, remote);

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
    protected void tearDown() throws Exception {
        super.tearDown();
        remote.getDatabase().close();
        local.getDatabase().close();
        // delete databases
        getContext().deleteDatabase("SyncProviderTest-Local");
        getContext().deleteDatabase("SyncProviderTest-Remote");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void test_sync_localToRemote() throws Exception {
        new Person(local, "Person1", 1).save();
        new Person(local, "Person2", 2).save();
        new Person(local, "Person3", 3).save();
        new Person(local, "Person4", 4).save();
        new Person(local, "Person5", 5).save();

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(0, remote.getDatabase().get("Person").count());

        provider.sync(syncTimestamp);

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());
    }

    public void test_sync_RemoteToLocal() throws Exception {
        new Person(remote, "Person1", 1).save();
        new Person(remote, "Person2", 2).save();
        new Person(remote, "Person3", 3).save();
        new Person(remote, "Person4", 4).save();
        new Person(remote, "Person5", 5).save();

        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        assertEquals(0, local.getDatabase().get("Person").count());

        provider.sync(syncTimestamp);

        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        assertEquals(5, local.getDatabase().get("Person").count());
    }

    public void test_sync_close() throws Exception {
        File remoteDb = new File(remote.getDatabase().getPath());
        provider.sync(syncTimestamp);
        assertTrue(remoteDb.exists());

        provider.cleanup();
        assertFalse(remoteDb.exists());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

}
