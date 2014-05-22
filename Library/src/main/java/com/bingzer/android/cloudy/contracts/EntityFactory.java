package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.entities.SyncableEntity;

/**
 * Created by Ricky on 5/20/2014.
 */
public interface EntityFactory {
    SyncableEntity createEntity(String tableName);
}
