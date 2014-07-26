package com.bingzer.android.cloudy;

import android.util.Log;

import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.dbv.IEnvironment;

class LocalConfiguration extends SettingEntity implements ILocalConfiguration {

    ////////////////////////////////////////////////////////////////////////////////////////

    LocalConfiguration(IEnvironment env){
        super(env);
    }

    /**
     * Seed all configs if it does not exists
     */
    public static void seedConfigs(IEnvironment env){
        if(!hasConfig(env, Version)){
            getConfig(env, Version).setValue(BuildConfig.VERSION_NAME).save();
            Log.i(TAG, "Seeding " + Version + " with value: " + getConfig(env, Version).getValue());
        }

        if(!hasConfig(env, LastSync)){
            getConfig(env, LastSync).setValue(0).save();
            Log.i(TAG, "Seeding " + Version + " with value: " + getConfig(env, Version).getValue());
        }
    }

}
