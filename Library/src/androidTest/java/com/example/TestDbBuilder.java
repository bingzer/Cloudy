package com.example;

import android.content.Context;

import com.bingzer.android.cloudy.SQLiteSyncBuilder;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEnvironment;

public class TestDbBuilder extends SQLiteSyncBuilder {
    private Context context;

    public TestDbBuilder(Context context){
        this.context = context;
    }

    @Override
    public void onModelCreate(IDatabase database, IDatabase.Modeling modeling) {
        super.onModelCreate(database, modeling);
        modeling.add("Person")
                .addPrimaryKey("Id")
                .addInteger("SyncId")
                .addText("Name")
                .addInteger("Age")
                .addText("Picture");
    }

    @Override
    public ISyncEntity onEntityCreate(IEnvironment environment, String tableName) {
        if(tableName.equalsIgnoreCase("Person"))
            return new Person(environment);
        return null;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
