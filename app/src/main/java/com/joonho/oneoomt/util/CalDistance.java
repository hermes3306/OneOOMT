package com.joonho.oneoomt.util;

import android.util.Log;

/**
 * Created by joonhopark on 2017. 9. 18..
 */

public class CalDistance {
    public double theta, dist;
    public double bef_lat, bef_long, cur_lat, cur_long;
    public static String TAG = "CalDistance";

    public CalDistance(double bef_lat, double bef_long, double cur_lat, double cur_long) {
        this.theta = 0;
        this.dist = 0;
        this.bef_lat = bef_lat;
        this.bef_long = bef_long;
        this.cur_lat = cur_lat;
        this.cur_long = cur_long;
    }

    public double getDistance() {
        theta = bef_long - cur_long;
        dist = Math.sin(deg2rad(bef_lat)) * Math.sin(deg2rad(cur_lat)) + Math.cos(deg2rad(bef_lat))
                * Math.cos(deg2rad(cur_lat))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist * 1000.0;  // 단위 Km 에서 m로 변환

        //Log.e(TAG, "Distance:" + dist);
        return dist; // 단위 m
    }

    private double deg2rad(double deg) {
        return (double)(deg*Math.PI/(double)180d);
    }

    private double rad2deg(double rad) {
        return (double)(rad * (double)180d / Math.PI);
    }

}
