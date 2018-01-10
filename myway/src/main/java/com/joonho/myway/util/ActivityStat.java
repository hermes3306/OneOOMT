package com.joonho.myway.util;

import java.util.Date;

/**
 * Created by nice9uy on 18. 1. 10.
 */

public class ActivityStat {
    public Date start;
    public Date end;
    public String duration;
    public double distanceM;
    public double distanceKm;
    public double minperKm;
    public int calories;


    public ActivityStat(Date start, Date end, String duration, double distanceM, double distanceKm, double minperKm, int calories) {
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.distanceM = distanceM;
        this.distanceKm = distanceKm;
        this.minperKm = minperKm;
        this.calories = calories;
    }
}
