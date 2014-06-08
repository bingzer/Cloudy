package com.bingzer.android.cloudy;

import android.util.Log;

import com.bingzer.android.Randomite;
import com.bingzer.android.Timespan;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.dbv.IEnvironment;

class LocalConfiguration extends SettingEntity {

    static final String TAG = "LocalConfiguration";
    static final String SETTING_REVISION = "Revision";
    static final String SETTING_LOCK_TIMEOUT = "LockTimeout";
    static final String SETTING_CLIENTID = "ClientId";
    static final String SETTING_VERSION = "Version";
    static final String SETTING_CREATED = "CreationDate";

    ////////////////////////////////////////////////////////////////////////////////////////

    LocalConfiguration(IEnvironment env){
        super(env);
    }

    /**
     * Seed all configs if it does not exists
     */
    public static void seedConfigs(IEnvironment env){
        ILocalConfiguration config;
        if(!hasConfig(env, SETTING_CLIENTID)){
            config = getConfig(env, SETTING_CLIENTID);
            config.setValue(Randomite.uniqueId());
            config.save();
            Log.i(TAG, "Seeding " + SETTING_CLIENTID + " with value: " + config.getValue());
        }

        if(!hasConfig(env, SETTING_LOCK_TIMEOUT)){
            config = getConfig(env, SETTING_LOCK_TIMEOUT);
            config.setValue(Timespan.MINUTES_30);
            config.save();
            Log.i(TAG, "Seeding " + SETTING_LOCK_TIMEOUT + " with value: " + config.getValue());
        }

        if(!hasConfig(env, LocalConfiguration.SETTING_REVISION)){
            config = getConfig(env, LocalConfiguration.SETTING_REVISION);
            config.setValue(0);
            config.save();
            Log.i(TAG, "Seeding " + SETTING_REVISION + " with value: " + config.getValue());
        }

        if(!hasConfig(env, SETTING_VERSION)){
            config = getConfig(env, SETTING_VERSION);
            config.setValue(BuildConfig.VERSION_NAME);
            config.save();
            Log.i(TAG, "Seeding " + SETTING_VERSION + " with value: " + config.getValue());
        }

        if(!hasConfig(env, SETTING_CREATED)){
            config = getConfig(env, SETTING_CREATED);
            config.setValue(Timespan.now());
            config.save();
            Log.i(TAG, "Seeding " + SETTING_VERSION + " with value: " + config.getValue());
        }
    }

}
