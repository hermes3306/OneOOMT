package com.joonho.myway.util;

/**
 * Created by joonhopark on 2017. 10. 18..
 */

public class CalBearing {
    public double bef_lat, bef_long, cur_lat, cur_long;

    public CalBearing(double startLat, double startLong, double endLat, double endLong) {
        bef_lat = startLat;
        bef_long = startLong;
        cur_lat = endLat;
        cur_long = endLong;
    }

    public double getBearing() {
        double dLong = cur_long - bef_long;
        double dPhi = Math.log(Math.tan(cur_lat/2.0+Math.PI/4.0)/Math.tan(bef_lat/2.0+Math.PI/4.0));
        if (Math.abs(dLong) > Math.PI) {
            if(dLong > 0.0) {
                dLong = -(2.0 * Math.PI - dLong);
            } else {
                dLong = (2.0 * Math.PI + dLong);
            }
        }
        double bearing = (Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
        return bearing + 180.0;
    }
}
