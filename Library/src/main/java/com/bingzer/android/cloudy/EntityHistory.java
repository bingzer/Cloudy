package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.IEntityHistory;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.cloudy.contracts.ISystemEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEnvironment;

import static com.bingzer.android.Timespan.now;

final class EntityHistory extends SyncEntity implements IEntityHistory {

    private int entityAction;
    private String entityName;
    private long entitySyncId;
    private long timestamp;

    ////////////////////////////////////////////////////////////////////////////////////

    private EntityHistory(){
        super();
    }

    EntityHistory(IEnvironment environment){
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getEntityAction() {
        return entityAction;
    }

    public void setEntityAction(int entityAction) {
        this.entityAction = entityAction;
    }

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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

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
        mapper.map("EntityAction", new Delegate.TypeInteger() {
            @Override
            public void set(Integer integer) {
                setEntityAction(integer);
            }

            @Override
            public Integer get() {
                return getEntityAction();
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

    public static void insert(ISyncEntity entity){
        if(entity instanceof ISystemEntity) return;

        EntityHistory history = new EntityHistory();
        history.setEntityAction(INSERT);
        history.setEntityName(entity.getTableName());
        history.setEntitySyncId(entity.getSyncId());
        history.setTimestamp(now());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

    public static void delete(ISyncEntity entity){
        if(entity instanceof ISystemEntity) return;

        EntityHistory history = new EntityHistory();
        history.setEntityAction(DELETE);
        history.setEntityName(entity.getTableName());
        history.setEntitySyncId(entity.getSyncId());
        history.setTimestamp(now());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

    public static void update(ISyncEntity entity){
        if(entity instanceof ISystemEntity) return;

        EntityHistory history = new EntityHistory();
        history.setEntityAction(UPDATE);
        history.setEntityName(entity.getTableName());
        history.setEntitySyncId(entity.getSyncId());
        history.setTimestamp(now());

        history.getEnvironment().getDatabase().get(TABLE_NAME).insert(history);
    }

}
