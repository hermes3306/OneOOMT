package com.joonho.runme.util;

import java.io.Serializable;


public class MyActivity {

    public double latitude;
    public double longitude;
    public double altitude;
    public String added_on;

    public MyActivity(double l1, double l2, double alt, String a) {
        this.latitude = l1;
        this.longitude = l2;
        this.altitude = alt;
        this.added_on = a;
    }

    public String toString() {
        return "("+ latitude+ "," + longitude+ "," + altitude + "," + added_on + ")";
    }

}

