package com.bingzer.android.cloudy.utils;

public final class Timespan {

    public static final long NEVER          = -1;


    public static final long SECONDS_1      = (long) 1000;
    public static final long SECONDS_15     = (long) 1.5e+4;
    public static final long SECONDS_30     = (long) 3e+4;
    public static final long MINUTES_1      = (long) 6e+4;
    public static final long MINUTES_10     = (long) 6e+5;
    public static final long MINUTES_30     = (long) 1.8e+6;
    public static final long HOURS_1        = (long) 3.6e+6;
    public static final long HOURS_2        = (long) 7.2e+6;
    public static final long HOURS_3        = (long) 1.08e+7;
    public static final long HOURS_4        = (long) 1.44e+7;
    public static final long HOURS_5        = (long) 1.8e+7;
    public static final long HOURS_6        = (long) 2.16e+7;
    public static final long HOURS_7        = (long) 2.52e+7;
    public static final long HOURS_8        = (long) 2.88e+7;
    public static final long HOURS_9        = (long) 3.24e+7;
    public static final long HOURS_12       = (long) 4.32e+7;
    public static final long DAYS_1         = (long) 8.64e+7;
    public static final long DAYS_2         = (long) 1.728e+8;
    public static final long WEEKS_1        = (long) 6.048e+8;
    public static final long WEEKS_2        = (long) 12.096e+8;
    public static final long MONTHS_1       = (long) 2.63e+9;
    public static final long MONTHS_2       = (long) 2.63e+9;
    public static final long YEARS_1        = (long) 3.15569e+10;

    public static long now(){
        return System.currentTimeMillis();
    }

}
