package com.bingzer.android.cloudy.contracts;

import android.database.Cursor;

import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.ITable;

/**
 * Created by Ricky on 5/20/2014.
 */
public interface IBaseEntity extends IEntity {
    void setId(long id);

    @Override
    long getId();

    long getSyncId();

    void setSyncId(long syncId);

    String getTableName();

    void save();

    void delete();

    void load();

    void load(long id);

    void loadBySyncId();

    void loadBySyncId(long syncId);

    void load(Cursor cursor);

    @Override
    void map(Mapper mapper);

    IEnvironment getEnvironment();
}
