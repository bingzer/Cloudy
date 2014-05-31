package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;
import com.example.Person;
import com.example.TestDbBuilder;

import java.util.UUID;

import static org.mockito.Mockito.mock;

public class SyncEntityTest extends AndroidTestCase {

    IDatabase db;
    long person1Id = -1, person2Id = -1;
    long person1SyncId = -1, person2SyncId = -1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        db = DbQuery.getDatabase("SyncEntityTest");
        db.open(1, new TestDbBuilder(getContext()));

        if(!db.get("Person").has("Name = ?", "Person1"))
            new Person("Person1", 1).save();
        if(!db.get("Person").has("Name = ?", "Person2"))
            new Person("Person2", 2).save();

        person1Id = db.get("Person").selectId("Name = ?", "Person1");
        person2Id = db.get("Person").selectId("Name = ?", "Person2");
        person1SyncId = db.get("Person").select("Name = ?", "Person1").query("SyncId");
        person2SyncId = db.get("Person").select("Name = ?", "Person2").query("SyncId");
    }

    public void test_getsetId(){
        Person person = new Person();
        person.setId(90890);
        assertEquals(90890, person.getId());
    }

    public void test_getsetSyncId(){
        Person person = new Person();
        person.setSyncId(12355);
        assertEquals(12355, person.getSyncId());
    }

    public void test_getEnvironment(){
        Person person = new Person();
        assertNotNull(person.getEnvironment());

        IEnvironment env = mock(IEnvironment.class);
        person = new Person(env);
        assertNotNull(person.getEnvironment());
        assertTrue(env == person.getEnvironment());
    }

    public void test_getEnvironemnt_getDatabase(){
        Person person = new Person();
        assertNotNull(person.getEnvironment().getDatabase());
        assertTrue(db == person.getEnvironment().getDatabase());
    }

    public void test_getTableName(){
        Person person = new Person();
        assertEquals("Person", person.getTableName());
    }

    public void test_save(){
        String name = UUID.randomUUID().toString();
        Person person = new Person(name, 30);
        assertEquals(-1, person.getId());

        assertTrue(person.save());
        assertNotSame(-1, person.getId());
    }

    public void test_delete(){
        Person person = new Person();
        person.load(person1Id);
        assertTrue(person.getId() > 0);

        assertTrue(person.delete());
        assertEquals(-1, person.getId());
        assertFalse(person.load(person1Id));

        assertFalse(person.delete());
    }

    public void test_load(){
        Person person = new Person();
        person.load(person1Id);

        assertEquals("Person1", person.getName());
        assertEquals(1, person.getAge());
        assertTrue(person.getId() > 0);

        // modify
        person.setName("Something else");
        person.setAge(123);
        assertNotSame("Person1", person.getName());
        assertNotSame(1, person.getAge());

        // refresh from db
        person.load();
        assertEquals("Person1", person.getName());
        assertEquals(1, person.getAge());
    }

    public void test_load_cursor(){
        Person person = new Person();
        person.load(db.get("Person").select("Name = ?", "Person2").query());

        assertEquals("Person2", person.getName());
        assertEquals(2, person.getAge());
    }

    public void test_load_syncId(){
        Person person = new Person();

        assertTrue(person.loadBySyncId(person1SyncId));
        assertEquals("Person1", person.getName());
        assertEquals(1, person.getAge());

        assertFalse(person.loadBySyncId(-1));
    }

    /////////////////////////////////////////////////////////////////

    int insertCallbackCounter = 0;
    public void test_insert_callbak(){
        Person person = new Person(){
            @Override
            protected void onBeforeInsert() {
                insertCallbackCounter++;
            }

            @Override
            protected void onAfterInsert() {
                insertCallbackCounter++;
            }
        };

        assertTrue(person.save());
        assertEquals(2, insertCallbackCounter);
    }

    int updateCallbackCounter = 0;
    public void test_update_callbak(){
        Person person = new Person(){
            @Override
            protected void onBeforeUpdate() {
                updateCallbackCounter++;
            }

            @Override
            protected void onAfterUpdate() {
                updateCallbackCounter++;
            }
        };

        assertTrue(person.load(person1Id));
        assertTrue(person.save());
        assertEquals(2, updateCallbackCounter);
    }

    int deleteCallbackCounter = 0;
    public void test_delete_callback(){
        Person person = new Person(){
            @Override
            protected void onBeforeDelete() {
                deleteCallbackCounter++;
            }

            @Override
            protected void onAfterDelete() {
                deleteCallbackCounter++;
            }
        };

        assertTrue(person.load(person1Id));
        assertTrue(person.delete());
        assertEquals(2, deleteCallbackCounter);
    }

    int loadCallbackCounter = 0;
    public void test_load_callback(){
        Person person = new Person(){
            @Override
            protected void onBeforeLoad() {
                loadCallbackCounter++;
            }

            @Override
            protected void onAfterLoad() {
                loadCallbackCounter++;
            }
        };
        assertTrue(person.load(person1Id));
        assertEquals(2, loadCallbackCounter);
    }

}
