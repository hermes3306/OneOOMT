package com.joonho.oneoomt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.util.ActivityUtil;
import com.joonho.oneoomt.util.CalDistance;
import com.joonho.oneoomt.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.joonho.oneoomt.RunningActivity.pTimerPeriod;

public class StartRunning2Activity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "StartRunning2Activity";

    private long start_time, end_time;
    private TextView tv_time_elapsed = null;
    private TextView tv_total_distance = null;
    private TextView tv_avg_pace = null;
    private TextView tv_cur_pace = null;
    private ImageButton imb_stop_timer = null;

    private TimerTask mTask = null;
    private Timer mTimer = null;

    private boolean isStarted = true;

    private double total_distance = 0;
    private Location start_loc;
    private ArrayList<Location> mList = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_running2);

        initialize_Location_Manager();


        // start information
        tv_time_elapsed = (TextView) findViewById(R.id.tv_time_elapsed);
        tv_total_distance = (TextView) findViewById(R.id.tv_total_distance);
        tv_avg_pace = (TextView) findViewById(R.id.tv_avg_pace02);
        tv_cur_pace = (TextView) findViewById(R.id.tv_cur_pace);
        imb_stop_timer = (ImageButton) findViewById(R.id.imb_stop_timer);

        doMyTimeTask();
    }

    LocationManager locationManager = null;
    Boolean isGPSEnabled = null;
    Boolean isNetworkEnabled = null;

    public void initialize_Location_Manager() {
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
//                Log.e(TAG,"New Loc " + lat + " " + lng);
//                //Toast.makeText(StartRunning2Activity.this, lat + " " + lng, Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if ((ContextCompat.checkSelfPermission(StartRunning2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunning2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunning2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunning2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunning2Activity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(StartRunning2Activity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public Location getLocation() {
        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLatitude();
                double lat = lastKnownLocation.getLatitude();
                Log.d(TAG, "GPS,  longtitude=" + lng + ", latitude=" + lat);
                return lastKnownLocation;
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public void doTimerPause() {
        isStarted = !isStarted;
        Log.e(TAG, "isStarted: " + isStarted);
        Toast.makeText(StartRunning2Activity.this, "isStarted = " + isStarted, Toast.LENGTH_LONG).show();
    }

    public void doMyTimeTask() {
        mTask =new StartRunning2Activity.MyTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 1000, pTimerPeriod); //delaytime(10sec), period(1sec)
        total_distance = 0;
        isStarted = true;
        start_time = new Date().getTime();
        mList = new ArrayList<Location>();
        start_loc = getLocation();
        mList.add(start_loc);
    }

    public void alertDialogChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartRunning2Activity.this);
        alertDialog.setTitle("운동모드 선택");
        alertDialog.setMessage("운동모드를 선택해주십시요.");

        String n_text = null;
        if(isStarted) n_text = "일시정지"; else n_text ="계속";

        alertDialog.setNeutralButton(n_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTimerPause();
            }
        });

        alertDialog.setPositiveButton("새로운시작", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<myActivity> mylist = ActivityUtil.Loc2Activity(mList);
                String fname = ActivityUtil.serializeWithCurrentTime(mylist);
                Toast.makeText(StartRunning2Activity.this, "" + fname + " 으로 저장후 다시 시작합니다.", Toast.LENGTH_LONG).show();

                doMyTimeTask();
            }
        });

        alertDialog.setNegativeButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isStarted = false;
                ArrayList<myActivity> mylist = ActivityUtil.Loc2Activity(mList);
                String fname = ActivityUtil.serializeWithCurrentTime(mylist);
                Toast.makeText(StartRunning2Activity.this, "" + fname + " 으로 저장후 종료합니다.", Toast.LENGTH_LONG).show();

                mTask.cancel();
                finish();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.imb_stop_timer:
                alertDialogChoice();
                break;
        }
    }

    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            if(!isStarted) return;
            long start = System.currentTimeMillis();
            StartRunning2Activity.this.runOnUiThread(new Runnable() {
                public void run() {

                    // duration
                    end_time = new Date().getTime();
                    long elapsed_time = end_time - start_time;
                    String duration = StringUtil.Duration(new Date(start_time), new Date(end_time));
                    tv_time_elapsed.setText(duration);
                    Log.e(TAG, "" + duration + "");

                    // distance
                    Location cur_loc = getLocation();
                    if(cur_loc == null) {

                    }else {
                        int msize = mList.size();
                        if(msize==0) {
                            mList.add(cur_loc);
                            return;
                        }

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
                            mList.add(cur_loc);
                        }

                        total_distance = total_distance + dist;
                        double dist_kilo = total_distance / 1000f;
                        String distance_str = String.format("%.2f", dist_kilo);
                        tv_total_distance.setText(distance_str);

                        long elaped_min = elapsed_time / 60;
                        double minpk = (double) (elaped_min / dist_kilo);
                        String minpk_str = String.format("%.2f", minpk);

                        int minpk_hour = (int)(minpk/3600f);
                        int minpk_min =  (int)((minpk - minpk_hour*3600f) /60f);
                        int minpk_sec =  (int)(minpk - minpk_hour*3600f - minpk_min * 60f);

                        if(minpk_hour != 0) minpk_str = String.format("%2d:%02d:%02d", minpk_hour, minpk_min, minpk_sec);
                        else                minpk_str = String.format("%2d:%02d", minpk_min, minpk_sec);
                        if(Double.isInfinite(minpk)) minpk_str = "--:--";
                        tv_avg_pace.setText(minpk_str);  //  00:00로 변경 필요
                    }
                }
            });

        }

    }


}

