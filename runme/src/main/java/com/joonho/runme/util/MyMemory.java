package com.joonho.runme.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Created by joonhopark on 2017. 12. 20..
 */

public class MyMemory implements Serializable {
    public static String TAG = "MyMemory";
    public static int GOOD_PLACE = 0;
    public static int GOOD_RESTAURANT = 1;
    public static int HAVE_BEEN = 2;
    public static int HAVE_EAT = 3;
    public static int TMP_PLACE = 4;

    public int type;
    public String name;
    public MyActivity loc;

    public LatLng getloc() {
        return new LatLng(loc.latitude, loc.longitude);
    }

    public String getaddr(Context ctx) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(loc.latitude, loc.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfo = addresses.get(0).getAddressLine(0).toString();
        }
        return addinfo;
    }

    public static void save() {

    }

}
