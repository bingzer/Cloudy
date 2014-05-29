package com.bingzer.android.cloudy.entities;

import com.bingzer.android.cloudy.contracts.IBaseEntity;
import com.bingzer.android.cloudy.contracts.IDataHistory;
import com.bingzer.android.cloudy.contracts.IEnvironment;
import com.bingzer.android.cloudy.contracts.ISystemEntity;
import com.bingzer.android.dbv.Delegate;

final class SyncHistory extends BaseEntity implements IDataHistory {

    public static final String TABLE_NAME = "SyncHistory";

    ////////////////////////////////////////////////////////////////////////////////////

    private int action;
    private String name;
    private long timestamp;

    ////////////////////////////////////////////////////////////////////////////////////

    private SyncHistory(){
        this(null);
    }

    SyncHistory(IEnvironment environment){
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void map(Mapper mapper) {
        super.map(mapper);

        mapper.map("Name", new Delegate.TypeString() {
            @Override
            public void set(String s) {
                setName(s);
            }

            @Override
            public String get() {
                return getName();
            }
        });
        mapper.map("Action", new Delegate.TypeInteger() {
            @Override
            public void set(Integer integer) {
                setAction(integer);
            }

            @Override
            public Integer get() {
                return getAction();
            }
        });
        mapper.map("Timestamp", new Delegate.TypeLong() {
            @Override
            public void set(Long aLong) {
                setTimestamp(aLong);
            }

            @Override
            public Long get() {
                return getTimestamp();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static void insert(IBaseEntity entity){
        if(entity instanceof ISystemEntity) return;

        SyncHistory history = new SyncHistory();
        history.setAction(INSERTED);
        history.setName(entity.getTableName());
        history.setSyncId(entity.getSyncId());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

    public static void delete(IBaseEntity entity){
        if(entity instanceof ISystemEntity) return;

        SyncHistory history = new SyncHistory();
        history.setAction(DELETED);
        history.setName(entity.getTableName());
        history.setSyncId(entity.getSyncId());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

    public static void update(IBaseEntity entity){
        if(entity instanceof ISystemEntity) return;

        SyncHistory history = new SyncHistory();
        history.setAction(UPDATED);
        history.setName(entity.getTableName());
        history.setSyncId(entity.getSyncId());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

}
