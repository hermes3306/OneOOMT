package com.joonho.oneoomt.file;

import java.io.Serializable;

/**
 * Created by user on 2017-10-30.
 */

public class myActivity2 implements Serializable {
    public double latitude;
    public double longitude;
    public double altitude;
    public String added_on;

    public myActivity2(double l1, double l2, double alt, String a) {
        this.latitude = l1;
        this.longitude = l2;
        this.altitude = alt;
        this.added_on = a;
    }

    public String toString() {
        return "("+ latitude+ "," + longitude+ "," + altitude + "," + added_on + ")";
    }
}
