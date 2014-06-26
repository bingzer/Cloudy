package com.bingzer.android.dbv;

/**
 * You must use this builder
 */
public abstract class OrmBuilder extends SQLiteBuilder{

    @Override
    public void onReady(IDatabase database) {
        IEnvironment local = Environment.getLocalEnvironment();
        local.setDatabase(database);
        local.setEntityFactory(getEntityFactory());

        super.onReady(database);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public abstract IEntityFactory getEntityFactory();
}
