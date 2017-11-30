package com.joonho.runme.util;

import java.io.Serializable;

/**
 * Created by jhpark on 17. 11. 30.
 */

public class myActivity implements Serializable {
    public double latitude;
    public double longitude;
    public String added_on;

    public myActivity(double l1, double l2, String a) {
        this.latitude = l1;
        this.longitude = l2;
        this.added_on = a;
    }

    public String toString() {
        return "("+ latitude+ "," + longitude+ "," + added_on + ")";
    }
}