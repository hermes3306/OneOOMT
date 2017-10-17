package com.joonho.oneoomt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.db.PropsDB;

import java.util.Random;

/**
 * Created by user on 2017-09-27.
 */

public class LocalLocationService extends Service {
    private static final String TAG = "LocalLocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private LocationManager mLocationManager = null;
    private final IBinder mBinder = new LocalBinder();
    private final Random mGenerator = new Random();
    private PropsDB pdb = new PropsDB();
    //private DBGateway dbgateway = new DBGateway();

    public class LocalBinder extends Binder {
        LocalLocationService getService() {
            return LocalLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    public Location getLastLocation() {
        for(int i=0;i<mLocationListeners.length;i++) {
            Location loc = mLocationListeners[i].lastLocation();
            if(loc != null) {
                Log.w(TAG, "~~~~~~ Last Location(" + loc + ") of " + loc.getProvider() );
                return loc;
            }
        }
        return null;
    }

    private class LocationListener implements android.location.LocationListener {
         Location mLastLocation = null;

        public Location lastLocation() {
            if (mLastLocation==null) return null;
            if (mLastLocation.getLatitude()==0.000000f || mLastLocation.getLongitude()== 0.000000f) return null;
            Log.w(TAG,"lastLocation() " + mLastLocation.getProvider() + " invoked! \n lastLocation() = " + mLastLocation);
            return mLastLocation;
        }

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            if(!isBetterLocation(location, mLastLocation)) {
                Toast.makeText(getApplicationContext(), "!isBetterLocation: " + location, Toast.LENGTH_SHORT).show();
                return;
            }

            if(location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                try {
                    if(pdb.getProperty(getApplicationContext(), "GPS_Listener").equalsIgnoreCase("false")) {
                        Log.e(TAG, "**** GPS data discarded from Setting ......" + location );
                        return;
                    }
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }

            if(location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                try {
                    if (pdb.getProperty(getApplicationContext(), "NETWORK_Listener").equalsIgnoreCase("false")) {
                        Log.e(TAG, "**** Network data discarded from Setting ......" + location);
                        return;
                    }
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }

            mLastLocation.set(location);

            try {
                String toastlc = pdb.getProperty(getApplicationContext(), "Toast_LocationChanged");

                if (toastlc.equalsIgnoreCase("true"))
                    Toast.makeText(getApplicationContext(), "onLocationChanged: " + location, Toast.LENGTH_SHORT).show();

            }catch(Exception e) {
                e.printStackTrace();
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

//    LocalLocationService.LocationListener[] mLocationListeners = new LocationListener[]{
//            new LocationListener(LocationManager.PASSIVE_PROVIDER)
//    };

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
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
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

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
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
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
}