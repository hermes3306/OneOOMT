package com.joonho.oneoomt.file;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by joonhopark on 2017. 10. 8..
 */

public class myPicture implements Serializable {
//    public double latitude;
//    public double longitude;
//    public String added_on;

    public myActivity myactivity;
    public String picname;
    public String filepath;


//    public myPicture(double _latitue, double _longitude, String _added_on, String _picname, String _filepath) {
//        this.latitude = _latitue;
//        this.longitude = _longitude;
//        this.added_on = _added_on;
//        this.picname = _picname;
//        this.filepath = _filepath;
//    }

    public myPicture(myActivity _myactivity, String _picname, String _filepath) {
        this.myactivity = _myactivity;
        this.picname = _picname;
        this.filepath = _filepath;
    }

    public String toString() {
        return "(" + this.myactivity + "," + this.picname + "," + this.filepath + ")";
    }

}

