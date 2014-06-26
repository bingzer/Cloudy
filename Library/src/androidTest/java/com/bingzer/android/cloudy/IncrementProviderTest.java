package com.bingzer.android.cloudy;

import android.database.Cursor;

import com.bingzer.android.Path;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.providers.SyncProvider;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.example.Person;
import com.example.TestDbBuilder;

import java.io.File;

public class IncrementProviderTest extends UsingExternalDriveTestCase {

    SyncProvider provider;
    IEnvironment remote;
    IEnvironment local;
    long syncTimestamp;
    File image1, image2, image3, image4, image5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        syncTimestamp = Timespan.now();
        IDatabase localDb = DbQuery.getDatabase("SyncProviderTest-Local");
        localDb.open(1, new TestDbBuilder(getContext()));
        IDatabase remoteDb = DbQuery.getDatabase("SyncProviderTest-Remote");
        remoteDb.open(1, new TestDbBuilder(getContext()));

        local = new Environment(localDb);
        remote = new Environment(remoteDb);

        manager = new SQLiteSyncManager(getContext(), local, remoteRoot);
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

    public void test_sync_close() throws Exception {
        File remoteDb = new File(remote.getDatabase().getPath());
        provider.sync(syncTimestamp);
        assertTrue(remoteDb.exists());

        provider.cleanup();
        assertFalse(remoteDb.exists());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void test_sync_create_localToRemote() throws Exception {
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

    public void test_sync_delete_localToRemote() throws Exception {
        test_sync_create_localToRemote();
        syncTimestamp = Timespan.now();

        Cursor cursor = local.getDatabase().get("Person").select("Name = ?", "Person2").query();
        Person p = new Person(local);
        if(cursor.moveToNext()) p.load(cursor);
        cursor.close();
        assertEquals("Person2", p.getName());
        p.delete();
        assertFalse(local.getDatabase().get("Person").has("Name = ?", "Person2"));

        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());
    }

    public void test_sync_create_remoteToLocal() throws Exception {
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

    public void test_sync_delete_remoteToLocal() throws Exception {
        test_sync_create_remoteToLocal();
        syncTimestamp = Timespan.now();

        Cursor cursor = remote.getDatabase().get("Person").select("Name = ?", "Person2").query();
        Person p = new Person(remote);
        if(cursor.moveToNext()) p.load(cursor);
        cursor.close();
        assertEquals("Person2", p.getName());
        p.delete();
        assertFalse(remote.getDatabase().get("Person").has("Name = ?", "Person2"));

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());
    }

    public void test_sync_create_localToRemote_images() throws Exception {
        if(remoteRoot.get("Person") != null){
            for(RemoteFile child : remoteRoot.get("Person").list()){
                child.delete();
            }
        }

        new Person(local, "Person1", 1, image1.getAbsolutePath()).save();
        new Person(local, "Person2", 2, image2.getAbsolutePath()).save();
        new Person(local, "Person3", 3, image3.getAbsolutePath()).save();
        new Person(local, "Person4", 4, image4.getAbsolutePath()).save();
        new Person(local, "Person5", 5, image5.getAbsolutePath()).save();

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(0, remote.getDatabase().get("Person").count());

        provider.sync(syncTimestamp);

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        // make sure files exists
        int counter = 0;
        Cursor cursor = local.getDatabase().get("Person").select().query();
        while(cursor.moveToNext()){
            Person p = new Person(local);
            p.load(cursor);

            String remoteFilename = p.getSyncId() + "." + p.getLocalFiles()[0].getName();
            assertNotNull(remoteRoot.get("Person").get(remoteFilename));
            ++counter;
        }
        cursor.close();
        assertEquals(5, counter);
    }

    public void test_sync_delete_localToRemote_images() throws Exception {
        test_sync_create_localToRemote_images();
        syncTimestamp = Timespan.now();

        Cursor cursor = local.getDatabase().get("Person").select("Name = ?", "Person2").query();
        Person p = new Person(local);
        if(cursor.moveToNext()) p.load(cursor);
        cursor.close();
        assertEquals("Person2", p.getName());
        p.delete();
        assertFalse(local.getDatabase().get("Person").has("Name = ?", "Person2"));

        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());

        // make sure files exists
        int counter = 0;
        cursor = local.getDatabase().get("Person").select().query();
        while(cursor.moveToNext()){
            p = new Person(local);
            p.load(cursor);

            String remoteFilename = p.getSyncId() + "." + p.getLocalFiles()[0].getName();
            assertNotNull(remoteRoot.get("Person").get(remoteFilename));
            ++counter;
        }
        cursor.close();
        assertEquals(4, counter);

        for(RemoteFile child : remoteRoot.get("Person").list()){
            child.delete();
        }
    }

    public void test_sync_create_remoteToLocal_images() throws Exception {
        Person person1 = new Person(remote, "Person1", 1, image1.getAbsolutePath());
        person1.save();
        Person person2 = new Person(remote, "Person2", 2, image2.getAbsolutePath());
        person2.save();
        Person person3 = new Person(remote, "Person3", 3, image3.getAbsolutePath());
        person3.save();
        Person person4 = new Person(remote, "Person4", 4, image4.getAbsolutePath());
        person4.save();
        Person person5 = new Person(remote, "Person5", 5, image5.getAbsolutePath());
        person5.save();
        // manually upload these images to the storageprovider
        remoteRoot.create("Person");
        remoteRoot.get("Person").create(new LocalFile(image1, null, person1.getSyncId() + "." + image1.getName()));
        remoteRoot.get("Person").create(new LocalFile(image2, null, person2.getSyncId() + "." + image2.getName()));
        remoteRoot.get("Person").create(new LocalFile(image3, null, person3.getSyncId() + "." + image3.getName()));
        remoteRoot.get("Person").create(new LocalFile(image4, null, person4.getSyncId() + "." + image4.getName()));
        remoteRoot.get("Person").create(new LocalFile(image5, null, person5.getSyncId() + "." + image5.getName()));

        // delete files on disk
        assertTrue(image1.delete());
        assertTrue(image2.delete());
        assertTrue(image3.delete());
        assertTrue(image4.delete());
        assertTrue(image5.delete());
        assertFalse(image1.exists());
        assertFalse(image2.exists());
        assertFalse(image3.exists());
        assertFalse(image4.exists());
        assertFalse(image5.exists());

        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        assertEquals(0, local.getDatabase().get("Person").count());

        ///////////////////////////////
        provider.sync(syncTimestamp);
        ///////////////////////////////

        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        assertEquals(5, local.getDatabase().get("Person").count());

        // make sure files exists
        int counter = 0;
        Cursor cursor = remote.getDatabase().get("Person").select().query();
        while(cursor.moveToNext()){
            Person p = new Person(remote);
            p.load(cursor);

            String remoteFilename = p.getSyncId() + "." + p.getLocalFiles()[0].getName();
            assertNotNull(remoteRoot.get("Person").get(remoteFilename));
            ++counter;
        }
        cursor.close();
        assertEquals(5, counter);
        // delete files on disk
        assertTrue(image1.exists());
        assertTrue(image2.exists());
        assertTrue(image3.exists());
        assertTrue(image4.exists());
        assertTrue(image5.exists());

        for(RemoteFile child : remoteRoot.get("Person").list()){
            child.delete();
        }
    }


    public void test_sync_delete_remoteToLocal_images() throws Exception {
        test_sync_create_remoteToLocal_images();
        syncTimestamp = Timespan.now();

        Cursor cursor = remote.getDatabase().get("Person").select("Name = ?", "Person2").query();
        Person p = new Person(remote);
        if(cursor.moveToNext()) p.load(cursor);
        cursor.close();
        assertEquals("Person2", p.getName());
        p.delete();
        assertFalse(remote.getDatabase().get("Person").has("Name = ?", "Person2"));

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(6, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(6, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(4, local.getDatabase().get("Person").count());
        assertEquals(4, remote.getDatabase().get("Person").count());

        // make sure files exists
        assertTrue(image1.exists());
        assertFalse(image2.exists());
        assertTrue(image3.exists());
        assertTrue(image4.exists());
        assertTrue(image5.exists());

        for(RemoteFile child : remoteRoot.get("Person").list()){
            child.delete();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////


    public void test_sync_update_localToRemote() throws Exception {
        Person person1 = new Person(local, "Person1", 1, image1.getAbsolutePath());
        person1.save();
        Person person2 = new Person(local, "Person2", 2, image2.getAbsolutePath());
        person2.save();
        Person person3 = new Person(local, "Person3", 3, image3.getAbsolutePath());
        person3.save();
        Person person4 = new Person(local, "Person4", 4, image4.getAbsolutePath());
        person4.save();
        Person person5 = new Person(local, "Person5", 5, image5.getAbsolutePath());
        person5.save();

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(0, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        ////////////////////////////////////////////////////////////////////////////////
        syncTimestamp = Timespan.now();
        // change all pictures to image1
        person1.setName(person1.getName() + "-Edited");
        person1.setPicture(image1.getAbsolutePath());
        person1.save();
        person2.setName(person2.getName() + "-Edited");
        person2.setPicture(image1.getAbsolutePath());
        person2.save();
        person3.setName(person3.getName() + "-Edited");
        person3.setPicture(image1.getAbsolutePath());
        person3.save();
        person4.setName(person4.getName() + "-Edited");
        person4.setPicture(image1.getAbsolutePath());
        person4.save();
        person5.setName(person5.getName() + "-Edited");
        person5.setPicture(null);
        person5.save();

        assertEquals(10, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(10, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(10, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        int counter = 0;
        // make sure files exists
        Cursor cursor = remote.getDatabase().get("Person").select().query();
        while(cursor.moveToNext()){
            ++counter;

            Person p = new Person(remote);
            p.load(cursor);

            if(p.getLocalFiles() != null){
                String remoteFilename = p.getSyncId() + "." + p.getLocalFiles()[0].getName();

                assertNotNull(remoteRoot.get("Person").get(remoteFilename));
            }
            else{
                // person #5 is updated with no image
                assertTrue(counter == 5);
            }
            assertEquals("Person" + counter + "-Edited", p.getName());
        }
        cursor.close();
        assertEquals(5, counter);
    }

    public void test_sync_update_remoteToLocal() throws Exception {
        Person person1 = new Person(local, "Person1", 1, image1.getAbsolutePath());
        person1.save();
        Person person2 = new Person(local, "Person2", 2, image2.getAbsolutePath());
        person2.save();
        Person person3 = new Person(local, "Person3", 3, image3.getAbsolutePath());
        person3.save();
        Person person4 = new Person(local, "Person4", 4, image4.getAbsolutePath());
        person4.save();
        Person person5 = new Person(local, "Person5", 5, image5.getAbsolutePath());
        person5.save();

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(0, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(0, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        ////////////////////////////////////////////////////////////////////////////////
        syncTimestamp = Timespan.now();
        // change all pictures to image1
        person1 = new Person(remote, person1.getId(), person1.getSyncId(), "Person1-Edited", 10, image1.getAbsolutePath());
        person1.save();
        person2 = new Person(remote, person2.getId(), person2.getSyncId(), "Person2-Edited", 20, image1.getAbsolutePath());
        person2.save();
        person3 = new Person(remote, person3.getId(), person3.getSyncId(), "Person3-Edited", 30, image1.getAbsolutePath());
        person3.save();
        person4 = new Person(remote, person4.getId(), person4.getSyncId(), "Person4-Edited", 40, image1.getAbsolutePath());
        person4.save();
        person5 = new Person(remote, person5.getId(), person5.getSyncId(), "Person5-Edited", 50, null);
        person5.save();

        RemoteFile remoteDir = remoteRoot.get("Person");
        if(remoteDir.get(person1.getSyncId() + "." + image1.getName()) == null)
            remoteDir.create(new LocalFile(image1, null, person1.getSyncId() + "." + image1.getName()));
        if(remoteDir.get(person2.getSyncId() + "." + image1.getName()) == null)
            remoteDir.create(new LocalFile(image1, null, person2.getSyncId() + "." + image1.getName()));
        if(remoteDir.get(person3.getSyncId() + "." + image1.getName()) == null)
            remoteDir.create(new LocalFile(image1, null, person3.getSyncId() + "." + image1.getName()));
        if(remoteDir.get(person4.getSyncId() + "." + image1.getName()) == null)
            remoteDir.create(new LocalFile(image1, null, person4.getSyncId() + "." + image1.getName()));
        if(remoteDir.get(person5.getSyncId() + "." + image1.getName()) == null)
            remoteDir.create(new LocalFile(image1, null, person5.getSyncId() + "." + image1.getName()));

        assertEquals(5, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(10, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());
        ///////////
        provider.sync(syncTimestamp);
        //////////
        assertEquals(10, local.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(10, remote.getDatabase().get(IEntityHistory.TABLE_NAME).count());
        assertEquals(5, local.getDatabase().get("Person").count());
        assertEquals(5, remote.getDatabase().get("Person").count());

        int counter = 0;
        // make sure files exists
        Cursor cursor = local.getDatabase().get("Person").select().query();
        while(cursor.moveToNext()){
            ++counter;

            Person p = new Person(local);
            p.load(cursor);

            if(p.getLocalFiles() != null){
                String remoteFilename = p.getSyncId() + "." + p.getLocalFiles()[0].getName();

                assertNotNull(remoteRoot.get("Person").get(remoteFilename));
            }
            else{
                // person# 5 has no image
                assertEquals(5, counter);
            }
            assertEquals("Person" + counter + "-Edited", p.getName());
            assertEquals(counter * 10, p.getAge());
        }
        cursor.close();
        assertEquals(5, counter);

        assertTrue(image1.exists());
        assertFalse(image2.exists());
        assertFalse(image3.exists());
        assertFalse(image4.exists());
        assertFalse(image5.exists());
    }

}
