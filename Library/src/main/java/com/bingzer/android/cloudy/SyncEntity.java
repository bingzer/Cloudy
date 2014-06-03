package com.bingzer.android.cloudy;

import com.bingzer.android.Randomite;
import com.bingzer.android.cloudy.contracts.ISyncEntity;
import com.bingzer.android.dbv.BaseEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEnvironment;

import java.io.File;

public abstract class SyncEntity extends BaseEntity implements ISyncEntity {

    protected long syncId = Randomite.uniqueId();

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
        if(syncId == -1)
            syncId = Randomite.uniqueId();
    }

    @Override
    protected void onAfterInsert() {
        EntityHistory.insert(this);
    }

    @Override
    protected void onAfterUpdate() {
        EntityHistory.update(this);
    }

    @Override
    protected void onAfterDelete() {
        EntityHistory.delete(this);
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
