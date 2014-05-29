package com.bingzer.android.cloudy.contracts;

import android.database.Cursor;

import com.bingzer.android.dbv.IEntity;


public interface IBaseEntity extends IEntity {

    @Override
    long getId();

    void setId(long id);

    long getSyncId();

    void setSyncId(long syncId);

    void save();

    void delete();

    void load();

    void load(long id);

    void loadBySyncId();

    void loadBySyncId(long syncId);

    void load(Cursor cursor);

    @Override
    void map(Mapper mapper);

    //////////////////////////////////////////////////////////////

    String getTableName();

    IEnvironment getEnvironment();
}
