package com.bingzer.android.cloudy;

import com.bingzer.android.Randomite;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.BaseEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.contracts.IEnvironment;

import java.io.File;

public abstract class SyncEntity extends BaseEntity implements ISyncEntity {

    private long syncId = -1;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SyncEntity(){
        super();
    }

    public SyncEntity(IEnvironment environment){
        super(environment);
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public File[] getLocalFiles(){
        return null;
    }

    @Override
    public final long getSyncId() {
        return syncId;
    }

    @Override
    public final void setSyncId(long syncId) {
        this.syncId = syncId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onBeforeInsert() {
        syncId = Randomite.uniqueId();
    }

    @Override
    protected void onAfterInsert() {
        CloudyHistory.insert(this);
    }

    @Override
    protected void onAfterUpdate() {
        CloudyHistory.update(this);
    }

    @Override
    protected void onAfterDelete() {
        CloudyHistory.delete(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final void loadBySyncId(){
        load(getSyncId());
    }

    @Override
    public final void loadBySyncId(long syncId){
        environment.getDatabase().get(getTableName()).select("SyncId = ?", syncId).query(this);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void map(Mapper mapper) {
        mapper.mapId(new Delegate.TypeId(this) {
            @Override
            public void set(Long value) {
                setId(value);
            }
        });
        mapper.map("SyncId", new Delegate.TypeLong() {
            @Override
            public void set(Long aLong) {
                setSyncId(aLong);
            }

            @Override
            public Long get() {
                return getSyncId();
            }
        });
    }

}
