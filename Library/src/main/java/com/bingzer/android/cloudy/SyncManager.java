package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DatabaseMapping;
import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.cloudy.contracts.EntityFactory;
import com.bingzer.android.cloudy.contracts.Remote;
import com.bingzer.android.cloudy.contracts.SyncProvider;
import com.bingzer.android.cloudy.dir.DirectorySyncProvider;
import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.api.ResultImpl;
import com.bingzer.android.driven.contracts.Delegate;
import com.bingzer.android.driven.contracts.Result;
import com.bingzer.android.driven.contracts.Task;

import static com.bingzer.android.driven.utils.AsyncUtils.doAsync;

public class SyncManager implements Remote {

    private Driven driven;
    private DirectoryTree root;
    private DatabaseMapping dbMapping;

    //////////////////////////////////////////////////////////////////////////////////////////

    public SyncManager(Driven driven){
        this.driven = driven;
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

    public Driven getDriven() {
        return driven;
    }

    public DirectoryTree getRoot() {
        return root;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    public Result<SyncException> sync() throws SyncException {
        ResultImpl<SyncException> result = new ResultImpl<SyncException>(false);
        try{


            result.setSuccess(true);
        }
        catch (Exception e){
            result.setException(new SyncException(e));
        }
        return result;
    }

    public void syncAsync(Task<Result<SyncException>> result){
        doAsync(result, new Delegate<Result<SyncException>>() {
            @Override
            public Result<SyncException> invoke() {
                return sync();
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////

}
