package com.joonho.runme.util;

import android.location.Location;
import java.io.Serializable;

public class MyPicture implements Serializable {
//    public double latitude;
//    public double longitude;
//    public String added_on;

    public MyActivity myactivity;
    public String picname;
    public String filepath;


//    public myPicture(double _latitue, double _longitude, String _added_on, String _picname, String _filepath) {
//        this.latitude = _latitue;
//        this.longitude = _longitude;
//        this.added_on = _added_on;
//        this.picname = _picname;
//        this.filepath = _filepath;
//    }

    public MyPicture(MyActivity _myactivity, String _picname, String _filepath) {
        this.myactivity = _myactivity;
        this.picname = _picname;
        this.filepath = _filepath;
    }

    public String toString() {
        return "(" + this.myactivity + "," + this.picname + "," + this.filepath + ")";
    }

}

