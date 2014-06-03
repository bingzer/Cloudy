package com.bingzer.android.cloudy;

import com.bingzer.android.Parser;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;

class LocalConfiguration extends SyncEntity implements ILocalConfiguration {

    static final String SETTING_REVISION = "Revision";
    static final String SETTING_LOCK_TIMEOUT = "Timeout";
    static final String SETTING_CLIENTID = "ClientId";

    //////////////////////////////////////////////////////////////////////////////////////////////

    private String name;
    private String value;

    LocalConfiguration(IEnvironment environment){
        super(environment);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(long value) {
        setValue(String.valueOf(value));
    }

    @Override
    public long getValueAsLong() {
        return Parser.parseLong(value, -1);
    }

    @Override
    public void setValue(boolean value) {
        setValue(String.valueOf(value));
    }

    @Override
    public boolean getValueAsBoolean() {
        return Parser.parseBoolean(value);
    }

    @Override
    public void setValue(int value) {
        setValue(String.valueOf(value));
    }

    @Override
    public int getValueAsInteger() {
        return Parser.parseInt(value, -1);
    }

    @Override
    public void setValue(double value) {
        setValue(String.valueOf(value));
    }

    @Override
    public double getValueAsDouble() {
        return Parser.parseDouble(value, -1);
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void map(IEntity.Mapper mapper) {
        super.map(mapper);

        mapper.map("Name", new Delegate.TypeString() {
            @Override
            public void set(String s) {
                setName(s);
            }

            @Override
            public String get() {
                return getName();
            }
        });

        mapper.map("Value", new Delegate.TypeString() {
            @Override public void set(String s) {
                setValue(s);
            }

            @Override public String get() {
                return getValue();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks to see if there's a config by {@code configName}
     */
    public static boolean hasConfig(IEnvironment env, String configName){
        ITable table = env.getDatabase().get(TABLE_NAME);
        ILocalConfiguration config = new LocalConfiguration(env);
        config.setName(configName);
        return table.has("Name = ?", configName);
    }

    /**
     * Returns a config object. If a config object does not exists, it will create one
     */
    public static ILocalConfiguration getConfig(IEnvironment env, String configName){
        ITable table = env.getDatabase().get(TABLE_NAME);
        ILocalConfiguration config = new LocalConfiguration(env);
        config.setName(configName);

        if(table.has("Name = ?", configName))
            table.select("Name = ?", configName).query(config);
        else
            config.save();

        return config;
    }

}
