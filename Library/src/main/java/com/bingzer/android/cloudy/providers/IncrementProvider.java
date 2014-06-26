package com.bingzer.android.cloudy.providers;

import com.bingzer.android.cloudy.contracts.ISyncManager;

class IncrementProvider extends AbsSyncProvider {

    IncrementProvider(ISyncManager manager){
        super(manager);
    }

    @Override
    protected String getName() {
        return "IncrementProvider";
    }


}
