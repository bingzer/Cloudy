package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DatabaseMapping;
import com.bingzer.android.cloudy.contracts.EntityFactory;

import java.util.ArrayList;
import java.util.List;

class DatabaseMappingImpl implements DatabaseMapping {

    private List<Info> list = new ArrayList<Info>();

    protected void addInfo(Info info){
        list.add(info);
    }

    public List<Info> getInfoList() {
        return list;
    }

    //////////////////////////////////////////////////////////////////////////////////////

    static class Info {

        private String name;
        private String localDb;
        private EntityFactory factory;

        Info(String name, String localDb, EntityFactory factory){
            this.name = name;
            this.localDb = localDb;
            this.factory = factory;
        }

        //////////////////////////////////////////////////////////////////////////////////////

        public String getName() {
            return name;
        }

        public String getLocalDb() {
            return localDb;
        }

        public EntityFactory getFactory() {
            return factory;
        }
    }

}
