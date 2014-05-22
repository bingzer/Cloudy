package com.bingzer.android.cloudy.entities;

import com.bingzer.android.cloudy.contracts.NoSync;
import com.bingzer.android.cloudy.sync.providers.db.SyncEnvironment;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.ITable;

import java.util.Date;

import static com.bingzer.android.cloudy.utils.Converter.parseBoolean;
import static com.bingzer.android.cloudy.utils.Converter.parseDouble;
import static com.bingzer.android.cloudy.utils.Converter.parseInt;
import static com.bingzer.android.cloudy.utils.Converter.parseLong;

final class SyncData extends BaseEntity implements NoSync {

    public static final String TABLE_NAME = "SyncData";

    public static final String SYNC_DATE = "LastSync";
    public static final String VERSION = "Version";

    ////////////////////////////////////////////////////////////////////////////////////////

    private String name;
    private String value;

    ////////////////////////////////////////////////////////////////////////////////////////

    private SyncData(){
        this(null);
    }

    SyncData(SyncEnvironment environment){
        super(environment);
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getValueAsLong(){
        return parseLong(getValue(), Long.MIN_VALUE);
    }

    public int getValueAsInt(){
        return parseInt(getValue(), Integer.MIN_VALUE);
    }

    public boolean getValueAsBoolean(){
        return parseBoolean(getValue());
    }

    public double getValueAsDouble(){
        return parseDouble(getValue(), Double.MIN_VALUE);
    }

    public Date getValueAsDate(){
        return new Date(getValueAsLong());
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void map(Mapper mapper) {
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
            @Override
            public void set(String s) {
                setValue(s);
            }

            @Override
            public String get() {
                return getValue();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    public static SyncData getData(String name){
        final SyncData syncData = new SyncData();
        ITable table = syncData.environment.getDatabase().get(TABLE_NAME);

        if(table.has("Name = ?", name)){
            table.select("Name = ?", name).query(syncData);
        }
        else{
            syncData.save();
        }

        return syncData;
    }
}
