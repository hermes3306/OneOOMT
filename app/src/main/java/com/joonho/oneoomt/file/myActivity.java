package com.joonho.oneoomt.file;

import java.io.Serializable;

/**
 * Created by user on 2017-09-12.
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAdded_on() {
        return added_on;
    }

    public void setAdded_on(String added_on) {
        this.added_on = added_on;
    }
}
