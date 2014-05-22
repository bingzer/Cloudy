package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.entities.SyncableEntity;
import com.bingzer.android.driven.gdrive.GoogleDrive;

@SuppressWarnings("ALL")
class TestUsage {
    void test(){
        SyncManager manager = new SyncManager(new GoogleDrive());

        DirectoryTree root = manager.mapRoot("remote", "local");
        root.addNode("", "");

        manager.mapDatabase("", "", new EntityFactory() {
            @Override
            public SyncableEntity createEntity(String tableName) {
                if (tableName.equals("ClassOne")) return new ClassOne();
                else if (tableName.equals("ClassTwo")) return new ClassTwo();
                return null;
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////

    private class ClassOne extends SyncableEntity {

        @Override
        public String getTableName() {
            return null;
        }
    }

    private class ClassTwo extends SyncableEntity {

        @Override
        public String getTableName() {
            return null;
        }
    }
}
