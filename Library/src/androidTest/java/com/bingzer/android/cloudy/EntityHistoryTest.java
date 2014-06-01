package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.Randomite;
import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;
import com.example.Person;
import com.example.TestDbBuilder;

public class EntityHistoryTest extends AndroidTestCase {
    ITable historyTable;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IDatabase db = DbQuery.getDatabase("EntityHistoryTest");
        db.open(1, new TestDbBuilder(getContext()));

        historyTable = db.get(IEntityHistory.TABLE_NAME);
        historyTable.delete();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void test_getter_setter(){
        EntityHistory history = new EntityHistory(Environment.getLocalEnvironment());
        history.setEntityAction(1);
        history.setEntityName("EntityName");
        history.setEntitySyncId(2);
        history.setId(3);
        history.setSyncId(4);
        history.setTimestamp(5);

        assertEquals(1, history.getEntityAction());
        assertEquals("EntityName", history.getEntityName());
        assertEquals(2, history.getEntitySyncId());
        assertEquals(3, history.getId());
        assertEquals(4, history.getSyncId());
        assertEquals(5, history.getTimestamp());
    }

    public void test_getTableName(){
        EntityHistory history = new EntityHistory(Environment.getLocalEnvironment());
        assertEquals(IEntityHistory.TABLE_NAME, history.getTableName());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void test_entityInsert(){
        assertTrue(historyTable.count() == 0);

        String uniqueName = Randomite.unique();
        int uniqueAge = Randomite.chooseFrom(0, 1000);

        Person person = new Person(uniqueName, uniqueAge);
        person.save();
        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.INSERT, "Person", person.getSyncId()));
    }

    public void test_entityDelete(){
        assertTrue(historyTable.count() == 0);

        String uniqueName = Randomite.unique();
        int uniqueAge = Randomite.chooseFrom(0, 1000);

        Person person = new Person(uniqueName, uniqueAge);
        person.save();

        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.INSERT, "Person", person.getSyncId()));
        assertFalse(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.DELETE, "Person", person.getSyncId()));

        person.delete();
        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.INSERT, "Person", person.getSyncId()));
        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.DELETE, "Person", person.getSyncId()));
    }

    public void test_entityUpdate(){
        assertTrue(historyTable.count() == 0);

        String uniqueName = Randomite.unique();
        int uniqueAge = Randomite.chooseFrom(0, 1000);

        Person person = new Person(uniqueName, uniqueAge);
        person.save();

        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.INSERT, "Person", person.getSyncId()));
        assertFalse(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.UPDATE, "Person", person.getSyncId()));

        person.save();
        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.INSERT, "Person", person.getSyncId()));
        assertTrue(historyTable.has("EntityAction = ? AND EntityName = ? AND EntitySyncId = ?", IEntityHistory.UPDATE, "Person", person.getSyncId()));
    }
}
