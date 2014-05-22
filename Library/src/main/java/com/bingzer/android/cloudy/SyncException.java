package com.bingzer.android.cloudy;

/**
 * Created by Ricky on 5/19/2014.
 */
public class SyncException extends RuntimeException {
    public SyncException(String message){
        super(message);
    }

    public SyncException(Exception baseException){
        super(baseException);
    }
}
