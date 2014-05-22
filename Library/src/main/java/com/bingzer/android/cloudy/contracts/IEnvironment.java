package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;

public interface IEnvironment {

    IDatabase getDatabase();

    ITable getSyncHistory();

    ITable getSyncData();

    EntityFactory getEntityFactory();
}
