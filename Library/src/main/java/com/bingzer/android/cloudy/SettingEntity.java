package com.bingzer.android.cloudy;

import com.bingzer.android.Parser;
import com.bingzer.android.cloudy.contracts.ILocalConfiguration;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEnvironment;
import com.bingzer.android.dbv.ITable;

/**
 * Default local configuration
 */
public class SettingEntity extends SyncEntity implements ILocalConfiguration {

    private String name;
    private String value;

    public SettingEntity(){}

    public SettingEntity(IEnvironment environment){
        super(environment);
    }

    public String getName() {
        return name;
    }

    public ILocalConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ILocalConfiguration setValue(long value) {
        setValue(String.valueOf(value));
        return this;
    }

    @Override
    public long getValueAsLong() {
        return Parser.parseLong(value, -1);
    }

    @Override
    public ILocalConfiguration setValue(boolean value) {
        setValue(String.valueOf(value));
        return this;
    }

    @Override
    public boolean getValueAsBoolean() {
        return Parser.parseBoolean(value);
    }

    @Override
    public ILocalConfiguration setValue(int value) {
        setValue(String.valueOf(value));
        return this;
    }

    @Override
    public int getValueAsInteger() {
        return Parser.parseInt(value, -1);
    }

    @Override
    public ILocalConfiguration setValue(double value) {
        setValue(String.valueOf(value));
        return this;
    }

    @Override
    public double getValueAsDouble() {
        return Parser.parseDouble(value, -1);
    }

    @Override
    public ILocalConfiguration setValue(String value) {
        this.value = value;
        return this;
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
        ILocalConfiguration config = new com.bingzer.android.cloudy.LocalConfiguration(env);
        config.setName(configName);
        return table.has("Name = ?", configName);
    }

    /**
     * Returns a config object. If a config object does not exists, it will create one
     */
    public static ILocalConfiguration getConfig(IEnvironment env, String configName){
        ITable table = env.getDatabase().get(TABLE_NAME);
        ILocalConfiguration config = new com.bingzer.android.cloudy.LocalConfiguration(env);
        config.setName(configName);

        if(table.has("Name = ?", configName))
            table.select("Name = ?", configName).query(config);
        else
            config.save();

        return config;
    }

}
