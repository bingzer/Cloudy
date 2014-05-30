package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IDatabase;

public interface IEnvironment {

    void setDatabase(IDatabase db);

    IDatabase getDatabase();

    void setEntityFactory(IEntityFactory factory);

    IEntityFactory getEntityFactory();

    /////////////////////////////////////////////////////////////////////////

    ICloudyHistory createCloudyHistory();

    ICloudyClient getClient(long clientId);

}
