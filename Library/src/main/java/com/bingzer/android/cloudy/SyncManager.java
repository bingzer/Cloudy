package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DatabaseMapping;
import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.contracts.Remote;
import com.bingzer.android.driven.Result;
import com.bingzer.android.driven.StorageProvider;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Task;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

public class SyncManager implements Remote {

    private StorageProvider provider;
    private DirectoryTree root;
    private DatabaseMapping dbMapping;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SyncManager(StorageProvider provider){
        this.provider = provider;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DirectoryTree mapRoot(String remoteName, String local) {
        return (root = new DirectoryTreeImpl(this, remoteName, local));
    }

    @Override
    public DatabaseMapping mapDatabase(String remoteName, String local, EntityFactory factory) {
        if(dbMapping == null)
            dbMapping = new DatabaseMappingImpl();
        ((DatabaseMappingImpl)dbMapping).addInfo(new DatabaseMappingImpl.Info(remoteName, local, factory));
        return dbMapping;
    }

    public StorageProvider getProvider() {
        return provider;
    }

    public DirectoryTree getRoot() {
        return root;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    public Result<SyncException> sync() throws SyncException {
        Result<SyncException> result = new Result<SyncException>(false);
        try{


            result.setSuccess(true);
        }
        catch (Exception e){
            result.setException(new SyncException(e));
        }
        return result;
    }

    public void syncAsync(Task<Result<SyncException>> task){
        doAsync(task, new Delegate<Result<SyncException>>() {
            @Override
            public Result<SyncException> invoke() {
                return sync();
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////

}
