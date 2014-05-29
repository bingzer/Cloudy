package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.ITable;

public interface IEnvironment {

    void setDatabase(IDatabase db);

    IDatabase getDatabase();

    void setEntityFactory(IEntityFactory factory);

    IEntityFactory getEntityFactory();

    /////////////////////////////////////////////////////////////////////////

    IDataHistory createDataHistory();

    IDataEntity createDataEntity();
}
