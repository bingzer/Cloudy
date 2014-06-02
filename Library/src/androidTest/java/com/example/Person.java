package com.example;

import com.bingzer.android.cloudy.SyncEntity;
import com.bingzer.android.dbv.Delegate;
import com.bingzer.android.dbv.IEnvironment;

import java.io.File;

public class Person extends SyncEntity {

    private String name;
    private String picture;
    private int age;

    public Person(){
        this(null, -1);
    }

    public Person(String name, int age){
        this.name = name;
        this.age = age;
    }

    public Person(IEnvironment environment){
        super(environment);
    }

    public Person(IEnvironment environment, String name, int age){
        this(environment, name, age, null);
    }

    public Person(IEnvironment environment, String name, int age, String picture){
        super(environment);
        this.name = name;
        this.age = age;
        this.picture = picture;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * The table name that this entity represents
     */
    @Override
    public String getTableName() {
        return "Person";
    }

    @Override
    public File[] getLocalFiles() {
        if(picture != null){
            return new File[]{ new File(picture) };
        }
        return null;
    }

    /**
     * Determines how to map column and the class variable.
     *
     * @param mapper the mapper object
     */
    @Override
    public void map(Mapper mapper) {
        super.map(mapper);
        mapper.map("Name", new Delegate.TypeString(){
            @Override
            public void set(String value) {
                setName(value);
            }
            @Override
            public String get() {
                return getName();
            }
        });

        mapper.map("Age", new Delegate.TypeInteger(){
            @Override
            public void set(Integer value) {
                setAge(value);
            }
            @Override
            public Integer get() {
                return getAge();
            }
        });

        mapper.map("Picture", new Delegate.TypeString() {
            @Override
            public void set(String value) {
                setPicture(value);
            }

            @Override
            public String get() {
                return getPicture();
            }
        });
    }
}
