package com.joonho.oneoomt.util;

/**
 * Created by user on 2017-10-17.
 */

public class MapUtils {
    public static double  getBearing(double startLat, double startLong, double endLat, double endLong) {
        startLat = Math.toRadians(43.682213);
        startLong = Math.toRadians(-70.450696);
        endLat = Math.toRadians(43.682194);
        endLong = Math.toRadians(-70.450769);

        double dLong = endLong - startLong;


        double dPhi = Math.log(Math.tan(endLat/2.0+Math.PI/4.0)/Math.tan(startLat/2.0+Math.PI/4.0));
        if (Math.abs(dLong) > Math.PI) {
            if(dLong > 0.0) {

                dLong = -(2.0 * Math.PI - dLong);
            } else {

                dLong = (2.0 * Math.PI + dLong);
            }
        }

        double bearing = (Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
        return bearing;

    }
}
