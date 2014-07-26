package com.bingzer.android.cloudy;

import com.bingzer.android.Randomite;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.BaseEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEnvironment;

public abstract class SyncEntity extends BaseEntity implements ISyncEntity {

    protected long syncId = Randomite.uniqueId();
    protected long lastUpdated = 0;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SyncEntity(){
        super();
    }

    public SyncEntity(IEnvironment environment){
        super(environment);
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final long getSyncId() {
        return syncId;
    }

    @Override
    public final void setSyncId(long syncId) {
        this.syncId = syncId;
    }

    @Override
    public final void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public final long getLastUpdated() {
        return lastUpdated;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onBeforeInsert() {
        lastUpdated = Timespan.now();
    }

    @Override
    protected void onBeforeUpdate() {
        lastUpdated = Timespan.now();
    }

    @Override
    protected void onBeforeDelete() {
        lastUpdated = Timespan.now();
    }

    @Override
    protected void onAfterDelete() {
        DeleteHistory.delete(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final boolean loadBySyncId(long syncId){
        environment.getDatabase().get(getTableName()).select("SyncId = ?", syncId).query(this);
        return this.syncId == syncId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void map(Mapper mapper) {
        mapId(mapper);
        mapper.map("SyncId", new Delegate.TypeLong() {
            @Override public void set(Long aLong) {
                setSyncId(aLong);
            }
            @Override public Long get() {
                return getSyncId();
            }
        });
        mapper.map("LastUpdated", new Delegate.TypeLong() {
            @Override public void set(Long aLong) {
                setLastUpdated(aLong);
            }
            @Override public Long get() {
                return getLastUpdated();
            }
        });
    }

}
