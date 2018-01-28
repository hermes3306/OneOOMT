package com.joonho.myway;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.joonho.myway.util.Config;
import com.joonho.myway.util.MyActivityUtil;
import com.joonho.myway.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class MyLocationService extends Service {

    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int    LOCATION_INTERVAL       = Config._location_interval;
    private static final float  LOCATION_DISTANCE       = 10f;     // 20 미터
    private static final int    TWO_MINUTES             = 1000 * 60 * 2;

    /* Global variables */
    private ArrayList<MyActivity> mList = null;
    public ArrayList<MyActivity> getMyAcitivityList() {
        return mList;
    }

    public MyActivity getLastLocation() {
        if(mList==null) return null;
        if(mList.size()==0) return null;
        return mList.get(mList.size()-1);
    }

    public MyActivity getLastLastLocation() {
        if(mList==null) return null;
        if(mList.size()<=1) return null;
        return mList.get(mList.size()-2);
    }

    public void addLocation(Location location) {
        if(location==null) return;
        if(mList==null) mList = new ArrayList<>();
        mList.add(new MyActivity(location.getLatitude(), location.getLongitude(), location.getAltitude(),LocTimeStr(location)));
    }

    IBinder mBinder = new MyBinder();
    class MyBinder extends Binder {
        MyLocationService getService() {
            return MyLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public String LocTimeStr(Location loc) {
            String added_on = StringUtil.DateToString(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
            return added_on;
        }

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if(location==null) return;
            Log.e(TAG, "onLocationChanged: " + location);
            if(!isBetterLocation(location, mLastLocation)) {
                Toast.makeText(getApplicationContext(), "!isBetterLocation: " + location, Toast.LENGTH_LONG).show();
                return;
            }

            mLastLocation.set(location);

            if(mList==null) {
                mList = new ArrayList<>();
            }

            mList.add(new MyActivity(location.getLatitude(), location.getLongitude(), location.getAltitude(),LocTimeStr(location)));
            if(Config._last_save_point == null) Config._last_save_point = new Date();

            Date _date = new Date();
            long _elapsed = _date.getTime() - Config._last_save_point.getTime();
            boolean _save_activity = false;

            switch(Config._save_interval) {
                case Config._SAVE_INTERVAL_MININUTE:
                    if(_elapsed >= Config._MINUTE) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_10MINUTEES:
                    if(_elapsed >= Config._10MINUTES) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_30MINUTEES:
                    if(_elapsed >= Config._30MINUTES) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_HOUR:
                    if(_elapsed >= Config._HOUR) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_6HOURS:
                    if(_elapsed >= Config._HOUR) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_12HOURS:
                    if(_elapsed >= Config._HOUR) _save_activity = true;
                    break;
                case Config._SAVE_INTERVAL_DAY:
                    if(_elapsed >= Config._DAY) _save_activity = true;
                    break;
            }

            if(_save_activity) {
                String _fname = StringUtil.DateToString(_date,Config._filename_fmt) + Config._default_ext;
                MyActivityUtil.serializeActivityIntoFile(mList, _fname );
                //Toast.makeText(getApplicationContext(), "saved: " + _fname, Toast.LENGTH_SHORT).show();

                Config._last_save_point = _date;

                if(Config._last_save_fname != null) {
                    File _lastfile = new File(Config._last_save_fname);
                    if(_lastfile.exists()) {
                        MyActivity _firstAct_of_lastfile = MyActivityUtil.deserializeFirstActivity(_lastfile);
                        if(MyActivityUtil.isSameActivity(_firstAct_of_lastfile, mList.get(0))) {
                           if(Config._delete_file_with_same_start) _lastfile.delete();
                            //Toast.makeText(getApplicationContext(), "saved: " + _fname , Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "**** saved:" + _fname + "removed:" + Config._last_save_fname );
                        } else {
                            //Toast.makeText(getApplicationContext(), "saved: " + _fname, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "**** saved:" + _fname);
                        }
                    } else{
                        //Toast.makeText(getApplicationContext(), "saved: " + _fname, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "**** saved:" + _fname);
                    }
                    Config._last_save_fname = _fname;
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER)
            //,new LocationListener(LocationManager.NETWORK_PROVIDER)
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,  // 밀리세컨
                    LOCATION_DISTANCE,  // 미터
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        /*
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );

            Toast.makeText(getApplicationContext(),
                    "NETWORK Update req: LOCATION INTERVAL" + LOCATION_INTERVAL +
                            ", LOCATION_DISTANCE " + LOCATION_DISTANCE,
                    Toast.LENGTH_LONG).show();

        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        */
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /* Laboratory Program */
    public String LocTimeStr(Location loc) {
        String added_on = StringUtil.DateToString(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
        return added_on;
    }

    public Location getLocation() {
        String locationProvider =  mLocationManager.GPS_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                Location location = lastKnownLocation;
                mList.add(new MyActivity(location.getLatitude(), location.getLongitude(), location.getAltitude(),LocTimeStr(location)));
                return location;
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return null;
    }
}