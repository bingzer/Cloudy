package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;


public class Environment implements IEnvironment {

    private static IEnvironment environment;
    public static IEnvironment getDefault(){
        if(environment == null)
            environment = new Environment();
        return environment;
    }

    private Environment(){

    }

    @Override
    public IDatabase getDatabase() {
        return null;
    }

    @Override
    public ITable getSyncHistory() {
        return null;
    }

    @Override
    public ITable getSyncData() {
        return null;
    }

    @Override
    public EntityFactory getEntityFactory() {
        return null;
    }


}
