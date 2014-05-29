package com.bingzer.android.cloudy.entities;

import com.bingzer.android.cloudy.contracts.IDataEntity;
import com.bingzer.android.cloudy.contracts.IDataHistory;
import com.bingzer.android.cloudy.contracts.IEntityFactory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.dbv.IDatabase;


public class Environment implements IEnvironment {

    private static IEnvironment environment;
    public static IEnvironment getLocalEnvironment(){
        if(environment == null)
            environment = new Environment();
        return environment;
    }

    /////////////////////////////////////////////////////////////////////////////////

    private IDatabase database = null;
    private IEntityFactory factory = null;

    private Environment(){
        this(null, null);
    }

    public Environment(IDatabase database, IEntityFactory factory){
        this.database = database;
        this.factory = factory;
    }

    /////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setDatabase(IDatabase db) {
        database = db;
    }

    public IDatabase getDatabase(){
        return database;
    }

    @Override
    public void setEntityFactory(IEntityFactory factory) {
        this.factory = factory;
    }

    public IEntityFactory getEntityFactory(){
        return factory;
    }

    @Override
    public IDataHistory createDataHistory() {
        return new SyncHistory(this);
    }

    @Override
    public IDataEntity createDataEntity() {
        return new SyncData(this);
    }


}
