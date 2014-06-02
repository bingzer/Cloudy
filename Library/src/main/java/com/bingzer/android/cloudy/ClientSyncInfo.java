package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.IClientSyncInfo;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.Environment;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;

final class ClientSyncInfo extends SyncEntity implements IClientSyncInfo {

    private long clientId;
    private long revision;

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
    public long getRevision() {
        return revision;
    }

    @Override
    public void setRevision(long revision) {
        this.revision = revision;
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
        mapper.map("Revision", new Delegate.TypeLong() {
            @Override
            public void set(Long l) {
                setRevision(l);
            }

            @Override
            public Long get() {
                return getRevision();
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
