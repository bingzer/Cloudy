package com.bingzer.android.cloudy;

import com.bingzer.android.Timespan;

public class TimeRange {
    public long from;
    public long to;
    public TimeRange(long from, long to){
        this.from = from - (Timespan.MINUTES_1 * 5);
        this.to = to + (Timespan.MINUTES_1 * 5);
    }
}
