package com.bingzer.android.cloudy;

import android.content.Context;

import com.bingzer.android.Parser;
import com.bingzer.android.Path;
import com.bingzer.android.Randomite;
import com.bingzer.android.cloudy.contracts.IClientRevision;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.bingzer.android.Stringify.isNullOrEmpty;

class ClientRevision extends SyncEntity implements IClientRevision {

    private static long INVALID_CLIENT_ID = -1;
    private File clientFile;
    private long clientId = INVALID_CLIENT_ID;
    private long revision;

    ClientRevision(IEnvironment environment){
        super(environment);
    }

    ClientRevision(Context context, IEnvironment environment){
        super(environment);
        clientFile = new File(context.getFilesDir(), "Cloudy.Client");
        try {
            clientId = generateUniqueId();
            if(clientId == INVALID_CLIENT_ID)
                throw new IOException("Failed to generate a unique id for this client");

            IClientRevision model = getClient(environment, clientId);
            loadBySyncId(model.getSyncId());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

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

    /*
    IClientRevision getClientSyncInfo(){
        IClientRevision clientSyncInfo = ClientRevision.getClient(environment, getClientId());
        clientSyncInfo.setClientId(getClientId());
        clientSyncInfo.save();
        return clientSyncInfo;
    }
    */

    private long generateUniqueId() throws IOException{
        if(clientFile.exists()){
            // read from file
            return readClientFile();
        }
        else{
            return writeClientFile();
        }
    }

    private long readClientFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(clientFile));
        String input = br.readLine();
        Path.safeClose(br);

        long uniqueId = INVALID_CLIENT_ID;
        if(!isNullOrEmpty(input)){
            uniqueId = Parser.parseLong(input, INVALID_CLIENT_ID);
            if(uniqueId == INVALID_CLIENT_ID)
                throw new IOException("Not a valid ClientId: " + input);
        }
        return uniqueId;
    }

    private long writeClientFile() throws IOException {
        Long uniqueId = Randomite.uniqueId();

        FileWriter fw = new FileWriter(clientFile);
        fw.write(uniqueId.toString());
        fw.close();

        return readClientFile();
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    protected static IClientRevision getClient(IEnvironment environment, long clientId){
        final ClientRevision syncData = new ClientRevision(environment);
        syncData.setClientId(clientId);
        ITable table = environment.getDatabase().get(TABLE_NAME);

        if(table.has("ClientId = ?", clientId)){
            table.select("ClientId = ?", clientId).query(syncData);
        }
        else{
            syncData.save();
        }

        return syncData;
    }

}
