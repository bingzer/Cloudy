package com.bingzer.android.cloudy.utils;

import java.util.UUID;

/**
 * Created by Ricky on 5/19/2014.
 */
public class UniqueId {

    public static long generateUniqueId(){
        return UUID.randomUUID().getMostSignificantBits();
    }

}
