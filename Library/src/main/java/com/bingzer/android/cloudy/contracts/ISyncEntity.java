package com.bingzer.android.cloudy.contracts;


import com.bingzer.android.dbv.contracts.IBaseEntity;

import java.io.File;

public interface ISyncEntity extends IBaseEntity {

    File[] getLocalFiles();

    void loadBySyncId();

    void loadBySyncId(long syncId);
}
