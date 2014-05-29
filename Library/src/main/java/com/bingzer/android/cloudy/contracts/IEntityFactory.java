package com.bingzer.android.cloudy.contracts;

/**
 * Created by Ricky on 5/20/2014.
 */
public interface IEntityFactory {
    IBaseEntity createEntity(String tableName);
}
