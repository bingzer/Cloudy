package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.dbv.IBaseEntity;

import java.io.File;

public interface ISyncEntity extends IBaseEntity {

    File[] getLocalFiles();

    void setSyncId(long syncId);

    long getSyncId();

    void loadBySyncId();

    void loadBySyncId(long syncId);
}
