package com.joonho.runme.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nice9 on 2017-12-07.
 */

public class MapUtil {
    public static String TAG = "MapUtil";

    public static String getAddress(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfo = addresses.get(0).getAddressLine(0).toString();

//
//                Log.e(TAG + i + ":getAdminArea=", ""+addresses.get(i).getAdminArea());
//                Log.e(TAG + i + ":getSubAdminArea=", ""+addresses.get(i).getSubAdminArea());
//                Log.e(TAG + i + ":getSubLocality=", ""+addresses.get(i).getSubLocality());
//                Log.e(TAG + i + ":getSubThoroughfare=", ""+addresses.get(i).getSubThoroughfare());
//                Log.e(TAG + i + ":getFeatureName=", ""+addresses.get(i).getFeatureName());
//                Log.e(TAG + i + ":getLocality=", ""+addresses.get(i).getLocality());
//                Log.e(TAG + i + ":getPhone=", ""+addresses.get(i).getPhone());
//                Log.e(TAG + i + ":getPostalCode=", ""+addresses.get(i).getPostalCode());
//                Log.e(TAG + i + ":getPremises=", ""+addresses.get(i).getPremises());
//                Log.e(TAG + i + ":getUrl=", ""+addresses.get(i).getUrl());
//                Log.e(TAG + i + ":getAddressLine(0)=", ""+addresses.get(i).getAddressLine(0));
//                Log.e(TAG + i + ":getAddressLine(1)=", ""+addresses.get(i).getAddressLine(1));
//                Log.e(TAG + i + ":getAddressLine(2)=", ""+addresses.get(i).getAddressLine(2));
//                Log.e(TAG + i + ":getAddressLine(3)=", ""+addresses.get(i).getAddressLine(3));
//                Log.e(TAG + i + ":getThoroughfare)=", ""+addresses.get(i).getThoroughfare());
//            }

        }
        return addinfo;
    }

    public static String getAddressDong(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfoDong = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfoDong = addresses.get(0).getThoroughfare() +
                    (addresses.get(0).getPremises()==null?"":" " + addresses.get(0).getPremises());
        }
        return addinfoDong;
    }

    public static ArrayList<String> getAllAddresses(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            ArrayList<String> list = new ArrayList<String>();
            for(int i=0;i<addresses.size();i++) {
                list.add(addresses.get(i).getAddressLine(0).toString());
            }
            return list;
        }
        return null;
    }


}
