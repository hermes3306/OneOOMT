package com.joonho.runme;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joonho.runme.util.ActivityUtil;
import com.joonho.runme.util.MyActivity;
import com.joonho.runme.util.CalDistance;
import com.joonho.runme.util.MyNotifier;
import com.joonho.runme.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";

    private long start_time, end_time;
    private TextView tv_time_elapsed = null;
    private TextView tv_total_distance = null;
    private TextView tv_avg_pace = null;
    private TextView tv_cur_pace = null;
    private ImageButton imb_stop_timer = null;

    private TimerTask mTask = null;
    private Timer mTimer = null;

    private boolean isStarted = false;
    private double total_distance = 0;
    private long start = System.currentTimeMillis();

    private double paces[] = new double[1000]; //upto 1000 km
    private long   startime_paces[] = new long[1000]; // upto 1000 start time

    private Location start_loc;
    private ArrayList<Location> mList = new ArrayList<Location>();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.e(TAG,"------- onSaveInstanceState() called");


        savedInstanceState.putDouble("total_distance", total_distance);
        savedInstanceState.putBoolean("isStarted", isStarted);
        savedInstanceState.putLong("start", start);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"------- onCreate() called");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {

            Log.e(TAG,"------- savedInstanceState != null ");

            isStarted = savedInstanceState.getBoolean("isStarted");
            total_distance = savedInstanceState.getDouble("total_distance");
            start = savedInstanceState.getLong("start");
        } else {

            Log.e(TAG,"------- savedInstanceState == null ");


            initialize_Location_Manager();
            tv_time_elapsed = (TextView) findViewById(R.id.tv_time_elapsed);
            tv_total_distance = (TextView) findViewById(R.id.tv_total_distance);
            tv_avg_pace = (TextView) findViewById(R.id.tv_avg_pace02);
            tv_cur_pace = (TextView) findViewById(R.id.tv_cur_pace);
            imb_stop_timer = (ImageButton) findViewById(R.id.imb_stop_timer);
            doMyTimeTask();
        }
    }

    @Override
    public void onBackPressed() {
       Log.e(TAG,"------- onBackPressed() called");
       //super.onBackPressed();
    }

    @Override
    protected void onStart() {
        Log.e(TAG,"------- onStart() called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG,"------- onStop() called");
        super.onStop();
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
                //Log.e(TAG,"New Loc " + lat + " " + lng);
                //Toast.makeText(StartRunning2Activity.this, lat + " " + lng, Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };

        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
        (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
        (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
        (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
        (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
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
        //Toast.makeText(MainActivity.this, "isStarted = " + isStarted, Toast.LENGTH_LONG).show();
    }

    public void doMyTimeTask() {
        mTask =new MainActivity.MyTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 1000, 1000);  // 10초
        total_distance = 0;
        isStarted = true;
        start_time = new Date().getTime();
        mList = new ArrayList<Location>();
        start_loc = getLocation();
        if(start_loc != null) mList.add(start_loc);
    }

    public void alertDialogChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Activity Mode");
        alertDialog.setMessage("Choose Activity Mode:");

        String n_text = null;
        if(isStarted) n_text = "PAUSE"; else n_text ="CONTINUE";

        alertDialog.setNeutralButton(n_text, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int i) {
                doTimerPause();
                }
                });

        alertDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialogInterface, int i) {
            ArrayList<MyActivity> mylist = ActivityUtil.Loc2Activity(mList);
            String fname = ActivityUtil.serializeWithCurrentTime(mylist);
            Toast.makeText(MainActivity.this, "" + fname + " saved ...", Toast.LENGTH_LONG).show();
        }
        });

        alertDialog.setNegativeButton("NEW", new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface dialogInterface, int i) {
            ArrayList<MyActivity> mylist = ActivityUtil.Loc2Activity(mList);
            String fname = ActivityUtil.serializeWithCurrentTime(mylist);
            Toast.makeText(MainActivity.this, "" + fname + " created and started new activity...", Toast.LENGTH_LONG).show();
            doMyTimeTask();
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
       int lastkm=0;
       int lastmin=0;
       public void run() {
            if(!isStarted) return;
            start = System.currentTimeMillis();

            MainActivity.this.runOnUiThread(new Runnable() {

            public void run() {
                end_time = new Date().getTime();
                long elapsed_time = end_time - start_time;

//                Log.e(TAG, "start time:" + start_time);
//                Log.e(TAG, "elapsed time:" + elapsed_time);

                double cur_dist = 0;
                long cur_elapsed_time = 0;

                String duration = StringUtil.Duration(new Date(start_time), new Date(end_time));
                tv_time_elapsed.setText(duration);

                Location cur_loc = getLocation();
                if(cur_loc == null) {

                }else {
                    if(mList.size()==0) {
                        mList.add(cur_loc);
                        total_distance = 0;
                    } else{
                        Location last_loc = mList.get(mList.size() -1);
                        CalDistance cd = new CalDistance(last_loc.getLatitude(), last_loc.getLongitude(), cur_loc.getLatitude(), cur_loc.getLongitude());
                        if(!Double.isNaN(cd.getDistance())) {
                            cur_dist = cd.getDistance();
                            //cur_elapsed_time = last_loc.getTime() - cur_loc.getTime();
                            cur_elapsed_time = 1000l; // 1sec

//                            Log.e(TAG,"cur_dist:" + cur_dist);
//                            Log.e(TAG,"cur_elapsed_time:" + cur_elapsed_time);

                            total_distance = total_distance + cur_dist;
                            mList.add(cur_loc);
                        }
                    }
                }

                double dist_kilo = total_distance / 1000f;
                if( (int)dist_kilo > lastkm ) {
                    lastkm++;
                    String alertmsg = "" + lastkm + " km를 활동하였습니다.";
                    MyNotifier.go(MainActivity.this, "100대명산알람",alertmsg);
                }


                String distance_str = String.format("%.2f", dist_kilo);
                tv_total_distance.setText(distance_str);

                double elapsed_time_sec = (double) (elapsed_time / 1000l);
                double km_per_sec = (double) (elapsed_time_sec / dist_kilo);
                String avg_pace = String.format("%2d:%02d", (int)(km_per_sec/60), (int)(km_per_sec%60));
                if(dist_kilo != 0) tv_avg_pace.setText(avg_pace);

                if( (int)(elapsed_time_sec / 60)  > lastmin ) {
                    lastmin++;
                    String alertmsg = "" + lastmin + " 분을 활동하였습니다.";
                    MyNotifier.go(MainActivity.this, "100대명산알람",alertmsg);
                }

                double cur_dist_kilo = cur_dist / 1000f;
                double cur_elapsed_time_sec = (double) (cur_elapsed_time / 1000l);
                double cur_km_per_sec = (double) (cur_elapsed_time_sec / cur_dist_kilo);
                String cur_pace = String.format("%2d:%02d", (int)(cur_km_per_sec/60), (int)(cur_km_per_sec%60));
                if(cur_dist_kilo != 0) tv_cur_pace.setText(cur_pace);
                }
            });
       }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.map:
                Intent intent = new Intent(MainActivity.this, CurActivity.class);

                ArrayList<MyActivity> myalist = ActivityUtil.Loc2Activity(mList);
                intent.putExtra("locations",myalist);

                startActivity(intent);
                return true;

            case R.id.files_d:
                ActivityUtil._default_ext = ".ser";
                File list[] = ActivityUtil.getFiles();
                if(list == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int msize = list.length;

                final CharSequence items[] = new CharSequence[msize];
                final String filepath[] = new String[msize];

                for(int i=0;i<msize;i++) {
                    items[i] = list[i].getName();
                    filepath[i] = list[i].getAbsolutePath();
                }
                //final CharSequence items[] = {" A "," B "};

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                //alertDialog.setIcon(R.drawable.window);
                alertDialog.setTitle("Select An Activity");
                alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        File afile = new File(filepath[index]);
                        ActivityUtil.showActivityAlertDialog(MainActivity.this, afile, index);
                    }
                });
                alertDialog.setNegativeButton("Back",null);
                AlertDialog alert = alertDialog.create();
                alert.show();
                return true;

            case R.id.files_a:

                ActivityUtil._default_ext = ".ser";
                File list2[] = ActivityUtil.getFiles();
                if(list2 == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int msize2 = list2.length;

                final CharSequence items2[] = new CharSequence[msize2];
                final String filepath2[] = new String[msize2];

                for(int i=0;i<msize2;i++) {
                    items2[i] = list2[i].getName();
                    filepath2[i] = list2[i].getAbsolutePath();
                }
                //final CharSequence items[] = {" A "," B "};

                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                //alertDialog.setIcon(R.drawable.window);
                alertDialog2.setTitle("Select An Activity");
                alertDialog2.setItems(items2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        File afile = new File(filepath2[index]);
                        Intent intent = new Intent(MainActivity.this, ActFileActivity.class);
                        intent.putExtra("file", afile.getAbsolutePath());
                        intent.putExtra("pos", index);
                        startActivity(intent);
                    }
                });
                alertDialog2.setNegativeButton("Back",null);
                AlertDialog alert2 = alertDialog2.create();
                alert2.show();
                return true;

            case R.id.upgrade:
                ActivityUtil._default_ext = ".ser";
                File uplist[] = ActivityUtil.getFiles();
                if(uplist == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int upsize = uplist.length;
                for(int i=0;i<upsize;i++) ActivityUtil.upgrade(uplist[i]);
                return true;

            case R.id.notify:
                MyNotifier.go(MainActivity.this, "Hello", "World");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}

