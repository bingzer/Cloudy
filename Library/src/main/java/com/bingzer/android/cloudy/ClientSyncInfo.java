package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;

final class ClientSyncInfo extends SyncEntity implements IClientSyncInfo {

    private long clientId;
    private long lastSync;

    ClientSyncInfo(IEnvironment environment){
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    @Override
    public long getLastSync() {
        return lastSync;
    }

    @Override
    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void map(IEntity.Mapper mapper) {
        super.map(mapper);

        mapper.map("ClientId", new Delegate.TypeLong() {
            @Override
            public void set(Long l) {
                setClientId(l);
            }

            @Override
            public Long get() {
                return getClientId();
            }
        });
        mapper.map("LastSync", new Delegate.TypeLong() {
            @Override
            public void set(Long l) {
                setLastSync(l);
            }

            @Override
            public Long get() {
                return getLastSync();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    protected static IClientSyncInfo getClient(IEnvironment environment, long clientId){
        final ClientSyncInfo syncData = new ClientSyncInfo(environment);
        ITable table = syncData.getEnvironment().getDatabase().get(TABLE_NAME);

        if(table.has("ClientId = ?", clientId)){
            table.select("ClientId = ?", clientId).query(syncData);
        }
        else{
            syncData.save();
        }

        return syncData;
    }
}
