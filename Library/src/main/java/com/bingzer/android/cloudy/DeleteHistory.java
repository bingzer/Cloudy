package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.IDeleteHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISystemEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEnvironment;

final class DeleteHistory extends SyncEntity implements IDeleteHistory {

    private String entityName;
    private long entitySyncId;

    ////////////////////////////////////////////////////////////////////////////////////

    DeleteHistory(IEnvironment environment){
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    public void setEntitySyncId(long syncId){
        this.entitySyncId = syncId;
    }

    @Override
    public long getEntitySyncId() { return entitySyncId; }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void map(Mapper mapper) {
        super.map(mapper);

        mapper.map("EntityName", new Delegate.TypeString() {
            @Override
            public void set(String s) {
                setEntityName(s);
            }

            @Override
            public String get() {
                return getEntityName();
            }
        });
        mapper.map("EntitySyncId", new Delegate.TypeLong() {
            @Override
            public void set(Long aLong) {
                setEntitySyncId(aLong);
            }

            @Override
            public Long get() {
                return getEntitySyncId();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static void delete(ISyncEntity entity){
        if(entity instanceof ISystemEntity) return;

        DeleteHistory history = new DeleteHistory(entity.getEnvironment());
        history.setEntityName(entity.getTableName());
        history.setEntitySyncId(entity.getSyncId());
        history.setLastUpdated(entity.getLastUpdated());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

}
