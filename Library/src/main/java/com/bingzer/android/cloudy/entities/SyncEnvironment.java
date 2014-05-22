package com.bingzer.android.cloudy.entities;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.entities.SyncData;
import com.bingzer.android.cloudy.entities.SyncHistory;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;

final class SyncEnvironment implements IEnvironment{

    private IDatabase database = null;
    private EntityFactory factory = null;

    ///////////////////////////////////////////////////////////////////////////////////////

    SyncEnvironment(IDatabase database, EntityFactory factory){
        this.database = database;
        this.factory = factory;
    }

    public IDatabase getDatabase(){
        if(database == null)
            throw new SyncException("Database is not set yet");
        return database;
    }

    public ITable getSyncHistory(){
        return database.get(SyncHistory.TABLE_NAME);
    }

    public ITable getSyncData(){
        return database.get(SyncData.TABLE_NAME);
    }

    public EntityFactory getEntityFactory(){
        return factory;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

}
