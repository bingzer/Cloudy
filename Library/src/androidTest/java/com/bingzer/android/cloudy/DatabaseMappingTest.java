package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.entities.SyncableEntity;

/**
 * Created by Ricky on 5/21/2014.
 */
public class DatabaseMappingTest extends AndroidTestCase {

    private SyncManager manager;
    private DatabaseMappingImpl mapping;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = new SyncManager(null);
        mapping = (DatabaseMappingImpl) manager.mapDatabase("db1", "local1", new EntityFactory() {
            @Override
            public SyncableEntity createEntity(String tableName) {
                if(tableName.equalsIgnoreCase("db1_table1"))
                    return new Db1Table1();
                else if(tableName.equalsIgnoreCase("db1_table2"))
                    return new Db1Table2();
                return null;
            }
        });
        manager.mapDatabase("db2", "local2", new EntityFactory() {
            @Override
            public SyncableEntity createEntity(String tableName) {
                return new Db2Table1();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////

    public void test_getInfos(){
        assertEquals(2, mapping.getInfoList().size());
    }

    public void test_getInfos_getName(){
        assertEquals("db1", mapping.getInfoList().get(0).getName());
        assertEquals("db2", mapping.getInfoList().get(1).getName());
    }

    public void test_getInfos_getLocalDb(){
        assertEquals("local1", mapping.getInfoList().get(0).getLocalDb());
        assertEquals("local2", mapping.getInfoList().get(1).getLocalDb());
    }

    public void test_getInfos_getFactory(){
        assertEquals("db1_table1", mapping.getInfoList().get(0).getFactory().createEntity("db1_table1").getTableName());
        assertEquals("db1_table2", mapping.getInfoList().get(0).getFactory().createEntity("db1_table2").getTableName());

        assertEquals("db2_table1", mapping.getInfoList().get(1).getFactory().createEntity("db2_table1").getTableName());
    }

    ////////////////////////////////////////////////////////////////////////////////

    class Db1Table1 extends SyncableEntity {

        @Override
        public String getTableName() {
            return "db1_table1";
        }
    }

    class Db1Table2 extends SyncableEntity {

        @Override
        public String getTableName() {
            return "db1_table2";
        }
    }

    class Db2Table1 extends SyncableEntity {

        @Override
        public String getTableName() {
            return "db2_table1";
        }
    }
}
