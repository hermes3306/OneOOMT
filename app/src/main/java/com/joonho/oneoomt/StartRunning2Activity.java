package com.joonho.oneoomt;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.joonho.oneoomt.file.ActivityStat;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.util.CalDistance;
import com.joonho.oneoomt.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.joonho.oneoomt.RunningActivity.mCurLoc;
import static com.joonho.oneoomt.RunningActivity.pTimerPeriod;

public class StartRunning2Activity extends AppCompatActivity {
    private String TAG = "StartRunning2Activity";
    private long start_time, end_time;
    private TextView tv_time_elapsed = null;
    private TextView tv_total_distance = null;
    private TextView tv_avg_pace = null;
    private TextView tv_cur_pace = null;

    private double total_distance = 0;
    private Location start_loc;
    private ArrayList<Location> mList = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_running2);


        // start information
        Date date = new Date();
        start_time =  date.getTime();
        tv_time_elapsed = (TextView)findViewById(R.id.tv_time_elapsed);
        tv_total_distance = (TextView)findViewById(R.id.tv_total_distance);
        tv_avg_pace = (TextView)findViewById(R.id.tv_avg_pace);
        tv_cur_pace = (TextView)findViewById(R.id.tv_cur_pace);


        start_loc = RunningActivity.mCurLoc;
        mList.add(start_loc);

        doMyTimeTask();

    }

    LocationManager locationManager =  null;
    Boolean isGPSEnabled = null;
    Boolean isNetworkEnabled = null;

    public Location getLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        Log.d(TAG, "isGPSEnabled=" + isGPSEnabled);
        Log.d(TAG, "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                Toast.makeText(StartRunning2Activity.this, lat + " " + lng, Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLatitude();
            double lat = lastKnownLocation.getLatitude();
            Log.d(TAG, "GPS,  longtitude=" + lng + ", latitude=" + lat);
            return lastKnownLocation;
        }

//        locationProvider = LocationManager.NETWORK_PROVIDER;
//        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//        if (lastKnownLocation != null) {
//            double lng = lastKnownLocation.getLatitude();
//            double lat = lastKnownLocation.getLatitude();
//            Log.d(TAG, "NETWORK,  longtitude=" + lng + ", latitude=" + lat);
//            return lastKnownLocation;
//        }

        return null;
    }


    public void doMyTimeTask() {
        TimerTask mTask =new StartRunning2Activity.MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, 1000, pTimerPeriod); //delaytime(10sec), period(1sec)
    }

    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            long start = System.currentTimeMillis();
            StartRunning2Activity.this.runOnUiThread(new Runnable() {
                public void run() {

                    // duration
                    end_time = new Date().getTime();
                    long elapsed_time = end_time - start_time;
                    String duration = StringUtil.Duration(new Date(start_time), new Date(end_time));
                    tv_time_elapsed.setText(duration);

                    // distance
                    Location cur_loc = getLocation();
                    if(cur_loc == null) {

                    }else {

                        int msize = mList.size();
                        Location last_loc = mList.get( msize - 1);

                        CalDistance cd = new CalDistance(last_loc.getLatitude(), last_loc.getLongitude(), cur_loc.getLatitude(), cur_loc.getLongitude());
                        double dist = cd.getDistance();
                        if(Double.isNaN(dist)) dist = 0;

                        if(cur_loc == last_loc) {
                            Log.e(TAG, "same location");
                        } else if (Double.isNaN(dist)) {
                            Log.e(TAG, "dist is NAN");
                        } else if(dist < 0.1d) { // 10 cm 이하 변동 사항
                            Log.e(TAG, "distance < 0.1meter discarded");
                        } else {

                            total_distance = total_distance + dist;
                            double dist_kilo = total_distance / 1000f;
                            String distance_str = String.format("%.2f", dist_kilo);
                            tv_total_distance.setText(distance_str);

                            long elaped_min = elapsed_time / 60;
                            double minpk = (double) (elaped_min / dist_kilo);
                            String minpk_str = String.format("%.2f", minpk);

                            tv_avg_pace.setText(minpk_str);  //  00:00로 변경 필요

                            mList.add(cur_loc);
                        }
                    }

                }
            });

        }

    }


}

