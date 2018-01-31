package com.joonho.myway;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joonho.myway.util.ActivityStat;
import com.joonho.myway.util.CalDistance;
import com.joonho.myway.util.Config;
import com.joonho.myway.util.HttpDownloadUtility;
import com.joonho.myway.util.HttpRequest;
import com.joonho.myway.util.JSONUtil;
import com.joonho.myway.util.MyActivityUtil;
import com.joonho.myway.util.StringUtil;
import com.joonho.myway.util.WeatherAPI;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RunActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "RunActivity";
    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    String backupdir = StringUtil.DateToString(new Date(), "yyyyMMdd");

    public  SharedPreferences mPref = null;
    public  SharedPreferences.Editor mEditor = null;

    private long start_time, end_time;
    private TextView tv_time_elapsed = null;
    private TextView tv_total_distance = null;
    private TextView tv_act_type = null;
    private TextView tv_avg_pace = null;
    private TextView tv_cur_temp = null;
    private TextView tv_address = null;
    private TextView tv_lat_lng_altitude = null;
    private TextView tv_message = null;
    private TextView tv_km = null;
    private TextView tv_pace = null;
    private TextView tv_time = null;

    private static WeatherAPI.Weather cur_weather = null;
    private static WeatherAPI.myWeather cur_myweather = null;
    private static String filesOnCloud[];
    private static Long filesizeOnCloud[];

    private ImageButton imb_stop_timer = null;

    private TimerTask mTask = null;
    private Timer mTimer = null;

    private  boolean isStarted = false;
    private  double total_distance = 0;
    private  String last_fname = null;
    private  long start = System.currentTimeMillis();
    private  int lastkm=0;
    private  int lastmin=0;
    private  int lasthour=0;
    private  double maxAltitude=0;

    private  int mode1 =  0; /* 0: elapsed, 1: start time, 2: end time */
    private  int mode2 =  0; /* 0: Diastance, 1: Speed Total, 2: Speed Current */
    private  boolean mode3 =  false;
    private  int mode4 =  0;
    private  boolean mode_noti = false;
    private boolean mode_low_battery = true;
    private boolean direct_db_update = false;

    private double paces[] = new double[1000]; //upto 1000 km
    private long   startime_paces[] = new long[1000]; // upto 1000 start time

    private MyActivity start_loc;
    private ArrayList<MyActivity> mList = null;

    private boolean         __svc_started           = false;
    private Intent __svc_Intent                     = null;
    MyLocationService       mMyLocationService      = null;

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder mb = (MyLocationService.MyBinder) service;
            mMyLocationService = mb.getService();
            __svc_started = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            __svc_started = false;
        }
    };

    public void initSharedPreferences(ArrayList<MyActivity> _malist,
                           double _total_distance,
                           boolean _isStarted,
                           long _start,
                           String _last_fname,
                           int _lastkm,
                           int _lastmin,
                           int _lasthour) {

        mList = _malist;
        total_distance = _total_distance;
        isStarted = _isStarted;
        start = start_time = _start;
        last_fname = _last_fname;
        lastkm = _lastkm;
        lastmin = _lastmin;
        lasthour = _lasthour;
        saveSharedPreferences();
    }

    public  void saveSharedPreferences(){
        mPref = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mPref.edit();
        mEditor.putFloat("total_distance", (float)total_distance);
        mEditor.putBoolean("isStarted", isStarted);
        mEditor.putLong("start", start);
        mEditor.putString("last_fname", last_fname);
        mEditor.putInt("lastkm", lastkm);
        mEditor.putInt("lastmin", lastmin);
        mEditor.putInt("lasthour", lasthour);
        mEditor.commit();
    }

    public  void show_message(String msg) {
        tv_message.setText(msg);
    }

    public void recalculate_total_distance() {
        total_distance = 0;
        for(int i=0;i<mList.size()-1;i++) {
            CalDistance cd = new CalDistance(mList.get(i).latitude,
                    mList.get(i).longitude,
                    mList.get(i+1).latitude,
                    mList.get(i+1).longitude);
            total_distance += cd.getDistance();
        }
    }

    public void loadSharedPreferences() {

        if(mList==null) {
            mPref = getSharedPreferences("setup", MODE_PRIVATE);
            isStarted = mPref.getBoolean("isStarted", true);
            total_distance = mPref.getFloat("total_distance", 0f);
            start = start_time = mPref.getLong("start", new Date().getTime());
            last_fname = mPref.getString("last_fname", null);
            lastkm = mPref.getInt("lastkm", 0);
            lastmin = mPref.getInt("lastmin", 0);
            lasthour = mPref.getInt("lasthour", 0);

            if(last_fname != null) {
                File lastFile = new File(mediaStorageDir, last_fname);
                mList = MyActivityUtil.deserializeActivity(lastFile);
                Log.e(TAG, "mList null -- Activities reloaded..... ");
            }
        }

        /* Total Distance - Recalcuration */
        if(mList != null) recalculate_total_distance();

        Log.e(TAG,"isStarted:" + isStarted);
        Log.e(TAG, "total_distance:" + total_distance);
        Log.e(TAG,"start:"+ new Date(start));
        Log.e(TAG,"last_fname:" + last_fname);
        Log.e(TAG,"lastkm:" + lastkm);
        Log.e(TAG,"lastmin:" + lastmin);
        Log.e(TAG,"lasthour:" + lasthour);
        Log.e(TAG, "#of Act:" + (mList==null? 0: mList.size()));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.e(TAG,"------- onSaveInstanceState() called");

        savedInstanceState.putDouble("total_distance", total_distance);
        savedInstanceState.putBoolean("isStarted", isStarted);
        savedInstanceState.putLong("start", start);
        savedInstanceState.putString("last_fname", last_fname);
        savedInstanceState.putInt("lastkm", lastkm);
        savedInstanceState.putInt("lastmin", lastmin);
        savedInstanceState.putInt("lasthour", lasthour);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"------- onCreate() called");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        if(savedInstanceState != null) {

            Log.e(TAG,"------- savedInstanceState != null ");

            isStarted = savedInstanceState.getBoolean("isStarted");
            total_distance = savedInstanceState.getDouble("total_distance");
            start = savedInstanceState.getLong("start");
            last_fname = savedInstanceState.getString("last_fname");
            lastkm = savedInstanceState.getInt("lastkm", lastkm);
            lastmin = savedInstanceState.getInt("lastmin", lastmin);
            lasthour = savedInstanceState.getInt("lasthour", lasthour);

        } else {

            Log.e(TAG,"------- savedInstanceState == null ");


            initialize_Location_Manager();
            tv_time_elapsed = (TextView) findViewById(R.id.tv_time_elapsed);
            tv_total_distance = (TextView) findViewById(R.id.tv_total_distance);
            tv_act_type = (TextView) findViewById(R.id.tv_act_type);
            tv_avg_pace = (TextView) findViewById(R.id.tv_avg_pace02);
            tv_cur_temp = (TextView) findViewById(R.id.tv_cur_temp);
            tv_address = (TextView) findViewById(R.id.tv_address);
            tv_lat_lng_altitude = (TextView) findViewById(R.id.tv_lat_lng_altitude);
            tv_message = (TextView) findViewById(R.id.tv_message);
            tv_km = (TextView) findViewById(R.id.tv_km);
            tv_pace = (TextView) findViewById(R.id.tv_pace);
            tv_time = (TextView) findViewById(R.id.tv_time);


            imb_stop_timer = (ImageButton) findViewById(R.id.imb_stop_timer);
            //doMyTimeTask(); 신규 활동이 아닌 과거 활동을 복원함.

            loadSharedPreferences();

            if(last_fname != null) {
                File lastFile = new File(mediaStorageDir, last_fname);
                mList = MyActivityUtil.deserializeActivity(lastFile);
                Log.e(TAG, "mList null -- Activities reloaded..... ");
                if(mList==null) mList = new ArrayList<MyActivity>();
            }
            doMyTimeTask();
        }
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG,"------- onBackPressed() called");
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        Log.e(TAG,"------- onStart() called");
        super.onStart();
        loadSharedPreferences();

        Intent myI = new Intent(this, MyLocationService.class);
        bindService(myI, conn, Context.BIND_AUTO_CREATE);

        doMyTimeTask();
        Toast.makeText(RunActivity.this,"SERVICE STARTED", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        Log.e(TAG,"------- onStop() called");
        super.onStop();
        saveSharedPreferences();
        if(__svc_started) {
            unbindService(conn);
            __svc_started = false;
        }
    }

    public String getCurAddress(Context ctx, Location loc) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(),1);
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

    LocationManager locationManager = null;
    Boolean isGPSEnabled = null;
    Boolean isNetworkEnabled = null;
    Boolean noGPS = false;
    Boolean locationChanged = false;

    public void initialize_Location_Manager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.e(TAG, "isGPSEnabled=" + isGPSEnabled);
        Log.e(TAG, "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                locationChanged = true;
                double lat = location.getLatitude();
                double lng = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };

        if ((ContextCompat.checkSelfPermission(RunActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(RunActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            noGPS = false;
        }catch(Exception e) {
            Toast.makeText(getApplicationContext(),"No GPS Provider... ", Toast.LENGTH_LONG).show();
        }
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public Location getLocation() {
        return null;
    }

    public Location getLocation_direct() {
        // 먼저 GPS를 통해서 위치 찾기
        String locationProvider =  LocationManager.GPS_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                //Log.e(TAG, "New Location Found from -  " + locationProvider);
                return lastKnownLocation;
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        // GPS로 못찾을 경우 NETWORK 위치 찾기
        locationProvider = LocationManager.NETWORK_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                //Log.e(TAG, "New Location Found from -  " + locationProvider);
                return lastKnownLocation;
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        // GPS/NETWORK 모두 못 찾을 경우 다른 APP의 네트웤을 통해서 위치 찾기
        locationProvider = LocationManager.PASSIVE_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                //Log.e(TAG, "New Location Found from -  " + locationProvider);
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
        //Toast.makeText(Main2Activity.this, "isStarted = " + isStarted, Toast.LENGTH_LONG).show();
    }

    public String LocTimeStr(MyActivity loc) {
        return loc.added_on;
    }

    public long Str2LocTime(String str) {
        Date date = StringUtil.StringToDate(str,"yyyy년MM월dd일_HH시mm분ss초");
        return date.getTime();
    }

        public void doMyTimeTask(long period) {
            if(mTask != null) mTask.cancel();
            mTask =new MyTimerTask();
            mTimer = new Timer();
            mTimer.schedule(mTask, 1000, period);

            int msize = 0;
            if(mList != null) msize = mList.size();
            if( isStarted  && (mList != null) && msize > 0) {
                start_loc = mList.get(0);
                start_time = start;
            } else {
                total_distance = 0;
                isStarted = true;
                start_time = new Date().getTime();
                mList = new ArrayList<MyActivity>();

                MyActivity sl = null;
                if(__svc_started) {
                    sl = mMyLocationService.getLastLocation();
                }
                if(sl != null) {
                    start_loc = new MyActivity(sl.latitude, sl.longitude, sl.altitude, LocTimeStr(sl));
                    mList.add(start_loc);
                    getMyWeather();

                    String caddr = MyActivityUtil.getAddress(RunActivity.this, sl);
                    tv_address.setText(caddr);
                    String lla = String.format("위도:%3.3f, 경도:%3.3f, 고도:%3.1f", sl.latitude, sl.longitude, sl.altitude);
                    tv_lat_lng_altitude.setText(lla);
                }
            }
    }

    public void doMyTimeTask() {
        doMyTimeTask(1000);
    }

    public void alertDialogChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunActivity.this);
        alertDialog.setTitle("Activity Mode");
        alertDialog.setMessage("Choose Activity Mode:");

        alertDialog.setNeutralButton("MAP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                String fname = ActivityUtil.serializeWithCurrentTime(mList);
//                Intent intent = new Intent(Main2Activity.this, ActFileActivity.class);
//                intent.putExtra("file", new File(mediaStorageDir,fname).getAbsolutePath());
//                intent.putExtra("pos", 0);
//                startActivity(intent);

                String fname = StringUtil.DateToString(new Date(), Config._filename_fmt);
                MyActivityUtil.serializeActivityIntoFile(mList, fname);
                Intent intent = new Intent(RunActivity.this, FileActivity.class);
                intent.putExtra("file", new File(mediaStorageDir,fname).getAbsolutePath());
                startActivity(intent);
            }
        });


        alertDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String fname = StringUtil.DateToString(new Date(), Config._filename_fmt);;
                MyActivityUtil.serializeActivityIntoFile(mList,fname);
                Toast.makeText(RunActivity.this, "" + fname + " saved ...", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.setNegativeButton("NEW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String fname = StringUtil.DateToString(new Date(), Config._filename_fmt);;;
                MyActivityUtil.serializeActivityIntoFile(mList,fname);
                Toast.makeText(RunActivity.this, "" + fname + " created and started new activity...", Toast.LENGTH_LONG).show();

                initSharedPreferences(null,
                        0,
                        false,
                        new Date().getTime(),
                        null,
                        0,
                        0,
                        0
                );
                doMyTimeTask();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public void disp_tv_time_elapsed() {
        end_time = new Date().getTime();
        String duration = StringUtil.Duration(new Date(start_time), new Date(end_time));
        if(mode1==0) tv_time_elapsed.setText(duration);
        else if(mode1==1) {
            String t_str = StringUtil.DateToString(new Date(start_time), "HH:mm:ss") ;
            tv_time_elapsed.setText(t_str);
        } else {
            String t_str = StringUtil.DateToString(new Date(end_time), "HH:mm:ss") ;
            tv_time_elapsed.setText(t_str);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_time_elapsed:
                mode1 = mode1+1;
                if(mode1==3) mode1=0;
                switch(mode1) {
                    case 0: tv_time.setText("TIME"); break;
                    case 1: tv_time.setText("START AT"); break;
                    case 2: tv_time.setText("END AT"); break;
                }
                disp_tv_time_elapsed();
                break;
            case R.id.tv_total_distance:

                mode2 = mode2+1;
                if(mode2==3) mode2 = 0;
                switch(mode2) {
                    case 0: tv_km.setText("KILOMETERS"); break;
                    case 1: tv_km.setText("KM/H (AVG)"); break;
                    case 2: tv_km.setText("KM/H (CUR)"); break;
                }
                recalculate_total_distance();
                break;
            case R.id.tv_lat_lng_altitude:
                mode3 = !mode3;
                break;
            case R.id.tv_cur_temp:
                getMyWeather();
                break;
            case R.id.tv_avg_pace02:
                mode4 = mode4+1;
                if(mode4==2) mode4=0;
                switch(mode4) {
                    case 0: tv_pace.setText("AVG PACE"); break;
                    case 1: tv_pace.setText("CUR PACE"); break;
                }
                break;
            case R.id.tv_act_type:
                Intent intent = new Intent(RunActivity.this, FileActivity.class);
                intent.putExtra("locations",mList);
                startActivity(intent);
                break;
            case R.id.tv_address:
                // 지도 보기 메뉴 실행 필요함
                double lan = mList.get(mList.size()-1).latitude;
                double lon = mList.get(mList.size()-1).longitude;
                Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+lan+","+lon);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case R.id.tv_message:
            case R.id.imb_stop_timer:
                alertDialogChoice();
                break;
        }
    }

    public double getSpeed_Km_per_h(double distancem, long startingtime) {
        double dist_kilo = distancem / 1000f;
        long elapsed_sec = (new Date().getTime() - startingtime) / 1000L;
        double km_per_hour = (double)(dist_kilo / ((elapsed_sec / 60f) / 60f));
        if(Double.isInfinite(km_per_hour)) return 0.0f;
        return km_per_hour;
    }

    public double getSpeed_Min_Per_Km(double distancem, long startingtime) {
        double dist_kilo = distancem / 1000f;
        long elapsed_sec = (new Date().getTime() - startingtime) / 1000L;
        double Sec_Per_Km = elapsed_sec / dist_kilo;
        double Min_Per_Km = Sec_Per_Km / 60f;
        if(Double.isInfinite(Min_Per_Km)) return 0.0f;
        return Min_Per_Km;
    }

    public class MyTimerTask extends TimerTask{
        public void run() {
            if(!isStarted) return;
            //start = System.currentTimeMillis(); 시작시간은 onCreate or App실행시 초기화됨.

            RunActivity.this.runOnUiThread(new Runnable() {

                public void run() {
                    double cur_dist = 0;
                    double cur_dist_in_10_sec = 0;
                    long cur_elapsed_time = 0;
                    long cur_elapsed_time_in_10_sec = 0;

                    disp_tv_time_elapsed();

                    /* get Weather every 10 minutes */
                    String[] t_str_array = {"00:00", "10:00", "20:00", "30:00", "40:00", "50:00"};
                    if(Arrays.asList(t_str_array).contains(StringUtil.DateToString(new Date(end_time), "mm:ss"))) {
                        getMyWeather();
                        Log.e(TAG, "getMyWeather() called ... ");
                        last_fname = StringUtil.DateToString(new Date(), Config._filename_fmt);;;
                        MyActivityUtil.serializeActivityIntoFile(mList,last_fname);
                        //doHttpFileUpload3(Main2Activity.this, last_fname);
                        Log.e(TAG, "File Saved... ");
                    }
                    if(cur_myweather != null) {
                        if(cur_myweather.getTemp() != 0.0f) {
                            tv_cur_temp.setText(String.format("%.1f", cur_myweather.getTemp()));
                        }
                    }

                    //Location cur_loc = getLocation();
                    MyActivity cur_loc = null;
                    if(__svc_started) {
                        cur_loc = mMyLocationService.getLastLocation();
                    }

                    if (cur_loc == null) return;

                    /* real address */
                    String cur_addr = MyActivityUtil.getAddress(RunActivity.this, cur_loc);
                    tv_address.setText(cur_addr);

                    /* lat, lon, alt*/
                    if(maxAltitude < cur_loc.altitude) maxAltitude = cur_loc.altitude;
                    String lla = String.format("%dth: LAT %3.1f LON %3.1f ALT %3.1f", mList.size(), cur_loc.latitude, cur_loc.longitude, cur_loc.altitude);
                    if(mode3) tv_lat_lng_altitude.setText(lla);
                    else tv_lat_lng_altitude.setText(String.format("%dth: ALT %3.1f/%3.1fM",mList.size(),cur_loc.altitude,maxAltitude));

                    /* distance calcuration */
                    if (mList == null) {
                        mList = new ArrayList<MyActivity>();
                        mList.add(new MyActivity(cur_loc.latitude, cur_loc.longitude, cur_loc.altitude,LocTimeStr(cur_loc)));
                        total_distance = 0;
                        return;
                    }
                    if(mList.size()==0) {
                        mList.add(new MyActivity(cur_loc.latitude, cur_loc.longitude, cur_loc.altitude,LocTimeStr(cur_loc)));
                        total_distance = 0;
                        return;
                    }

                    /* calculate from last_location to cur_location */
                    MyActivity last_loc = mList.get(mList.size()-1);
                    CalDistance cd = new CalDistance(last_loc.latitude, last_loc.longitude, cur_loc.latitude, cur_loc.longitude);
                    if (Double.isNaN(cd.getDistance())) return;

                    /* display distance or speed (current or total */
                    cur_dist = cd.getDistance();
                    total_distance = total_distance + cur_dist;
                    if(cur_dist > 0.001f) {
                        mList.add(new MyActivity(cur_loc.latitude, cur_loc.longitude, cur_loc.altitude, LocTimeStr(cur_loc)));
                        if(direct_db_update) {
                            String urlstr = "http://180.69.217.73:81/OneOOMT/insert.php?";
                            urlstr += "latitude=" + cur_loc.latitude;
                            urlstr += "&longitude=" + cur_loc.longitude;
                            urlstr += "&altitude=" + cur_loc.altitude;
                            urlstr += "&added_on=" + LocTimeStr(cur_loc);
                            try {
                                new HttpRequest().execute(new URL(urlstr));
                                Log.e(TAG,"DB update... OK");
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    }

                    /* 10초단위로 시간 측정 및 거리 측정 */
                    long section_start_time =0; /* 10초 단위 시작 시간 */
                    double section_distance = 0;
                    if(mList.size() > 10) {
                        int _t_pos = mList.size()-11;
                        do {
                            section_start_time = Str2LocTime(mList.get(_t_pos).added_on);
                            _t_pos++;
                        } while( _t_pos < mList.size() && (end_time - section_start_time) > 1000*10);

                        if(_t_pos >= mList.size()) {
                            section_start_time = end_time;
                            section_distance = 0;
                        }
                        else {
                            for (int i= _t_pos-1; i < mList.size()-1; i++) {
                                CalDistance _cd_ = new CalDistance(
                                        mList.get(i).latitude,
                                        mList.get(i).longitude,
                                        mList.get(i+1).latitude,
                                        mList.get(i+1).longitude
                                );
                                if (!Double.isNaN(_cd_.getDistance()))
                                    section_distance = section_distance + _cd_.getDistance();
                            }
                        }
                    }else {
                        int _t_pos = 0;
                        do {
                            section_start_time = Str2LocTime(mList.get(_t_pos).added_on);
                            _t_pos++;
                        } while( _t_pos < mList.size() && (end_time - section_start_time) > 1000*10);

                        if(_t_pos >= mList.size()) {
                            section_start_time = end_time;
                            section_distance=0;
                        }
                        else {
                            for (int i = _t_pos-1; i < mList.size()-1; i++) {
                                CalDistance _cd_ = new CalDistance(
                                        mList.get(i).latitude,
                                        mList.get(i).longitude,
                                        mList.get(i+1).latitude,
                                        mList.get(i+1).longitude
                                );
                                if (!Double.isNaN(_cd_.getDistance()))
                                    section_distance = section_distance + _cd_.getDistance();
                            }
                        }
                    }

                    switch(mode2) {
                        case 0: tv_total_distance.setText(String.format("%.2f",total_distance/1000f));
                                break;
                        case 1: tv_total_distance.setText(String.format("%.2f", getSpeed_Km_per_h(total_distance, start_time )));
                                break;
                        case 2: tv_total_distance.setText(String.format("%.2f", getSpeed_Km_per_h(section_distance, section_start_time)));
                                break;
                    }

                    switch(mode4) {
                        case 0: /* AVG PACE */
                            tv_avg_pace.setText(String.format("%.1f", getSpeed_Min_Per_Km(total_distance, start_time)));
                            break;
                        case 1: /* CUR PACE */
                            tv_avg_pace.setText(String.format("%.1f", getSpeed_Min_Per_Km(section_distance, section_start_time)));
                            break;
                    }


                    /* activity type setting based on current speed */
                    double current_speed = getSpeed_Km_per_h(section_distance, section_start_time);
                    String current_act_type = null;

                    if(current_speed > 500f )  current_act_type = "FLYING";
                    else if(current_speed > 80f) current_act_type = "DRIVING (+80km/h)";
                    else if(current_speed > 50f) current_act_type = "DRIVING (+50km/h)";
                    else if(current_speed > 20f) current_act_type = "DRIVING (+20km/h)";
                    else if(current_speed > 10f) current_act_type = "DRIVING (+10km/h)";
                    else if(current_speed > 9f)  current_act_type = "RUNNING (+9km/h)";
                    else if(current_speed > 7f)  current_act_type = "RUNNING (+7km/h)";
                    else if(current_speed > 5f)  current_act_type = "RUNNING (+5km/h)";
                    else if(current_speed > 4f)  current_act_type = "WALKING (+4km/h)";
                    else if(current_speed > 3f)  current_act_type = "WALKING (+3km/h)";
                    else if(current_speed > 2f)  current_act_type = "HIKING. (+2km/h)";
                    else if(current_speed > 1f)  current_act_type = "HIKING. (+1km/h)";
                    else if(current_speed > 0.5f)  current_act_type = "HIKING. (+0.5km/h)";
                    else if(current_speed > 0.1f)  current_act_type = "100-500m/h";
                    else if(current_speed > 0.05f) current_act_type = "+50m/h";
                    else if(current_speed > 0.01f) current_act_type = "+10m/h";
                    else if(current_speed > 0.001f) current_act_type = "REST...2";
                    else if(current_speed > 0.000f) current_act_type = "REST...1";
                    else current_act_type = "SLEEP";
                    tv_act_type.setText(current_act_type);
                } /* end of Run() */
            });
        }
    }

    public void alertUploadServerChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunActivity.this);
        alertDialog.setTitle("Choose Upload Servlet");

        final EditText ed = new EditText(alertDialog.getContext());
        ed.setTextSize(15);
        ed.setText("http://180.69.217.73:8080/OneOOMT/upload");
        alertDialog.setView(ed);

        alertDialog.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String url = ed.getText().toString();
                doHttpFileUploadAll(RunActivity.this, url);
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;

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
            case R.id.files_cloud:
                String urls[] = new String[1];
                urls[0] =  Config._listURL;
                filesOnCloud = getFilesOnCloud(RunActivity.this, urls);

                if(filesOnCloud == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Files on Cloud to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                if(filesOnCloud.length==0) {
                    Toast.makeText(getApplicationContext(), "ERR: No Files on Cloud to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int msize3 = filesOnCloud.length;
                final CharSequence items3[] = new CharSequence[msize3];
                for(int i=0;i<msize3;i++) {
                    long sz = filesizeOnCloud[i];
                    String _sz=null;
                    if(sz > 1024*1024) _sz = "" + sz / (1024 * 1024) + "MB";
                    else if(sz > 1024) _sz = "" + sz / (1024) + "KB";
                    else _sz = "" + sz + "B";
                    items3[i] = filesOnCloud[i] +  "(" + _sz + ")" ;
                }

                AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(this);
                //alertDialog.setIcon(R.drawable.window);
                alertDialog3.setTitle("Select an activity on the cloud("+filesOnCloud.length+")");
                alertDialog3.setItems(items3, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        Intent intent = new Intent(RunActivity.this, CloudFileActivity.class);
                        intent.putExtra("files", filesOnCloud);
                        intent.putExtra("pos", index);
                        startActivityForResult(intent, REQUEST_ACTIVITY_FILE_LIST);
                    }
                });
                alertDialog3.setNegativeButton("Back",null);
                AlertDialog alert3 = alertDialog3.create();
                alert3.show();
                return true;

            case R.id.sync_mobile2cloud:
            case R.id.uploadall2:
            case R.id.uploadall:
                alertUploadServerChoice();
                return true;

            case R.id.clear_useless_cloud_activity:
                return true;

            case R.id.dbupdate:
                return true;

            case R.id.dbupdateall:
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < mList.size(); i++) {
                        JSONObject json = new JSONObject();
                        json.put("lat", mList.get(i).latitude);
                        json.put("lon", mList.get(i).longitude);
                        json.put("alt", mList.get(i).altitude);
                        json.put("add", mList.get(i).added_on);
                        jsonArray.put(i, json);
                    }
                    JSONObject jsonActivities = new JSONObject();
                    jsonActivities.put("activities", jsonArray);

                    Log.e(TAG,jsonActivities.toString());
                    JSONUtil.postJSON("http://180.69.217.73:81/OneOOMT/jsonPost.php",jsonActivities );

                }catch(Exception e) {
                    Log.e(TAG,e.toString());
                }
                return true;

            case R.id.dododo:
                MyActivityUtil.dododo();
                return true;

            case R.id.files_a:
                File list2[] = MyActivityUtil.getFiles();
                if(list2 == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int msize2 = list2.length;

                final CharSequence items2[] = new CharSequence[msize2];
                final String filepath2[] = new String[msize2];

                for(int i=0;i<msize2;i++) {
                    long sz = list2[i].length();
                    String _sz=null;
                    if(sz > 1024*1024) _sz = "" + sz / (1024 * 1024) + "MB";
                    else if(sz > 1024) _sz = "" + sz / (1024) + "KB";
                    else _sz = "" + sz + "B";

                    items2[i] = list2[i].getName() + "(" + _sz + ")" ;
                    filepath2[i] = list2[i].getAbsolutePath();
                }
                //final CharSequence items[] = {" A "," B "};

                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                //alertDialog.setIcon(R.drawable.window);
                alertDialog2.setTitle("Select an activity on the mobile("+msize2+")");
                alertDialog2.setItems(items2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        File afile = new File(filepath2[index]);
                        Intent intent = new Intent(RunActivity.this, FileActivity.class);
                        intent.putExtra("file", afile.getAbsolutePath());
                        intent.putExtra("pos", index);
                        startActivityForResult(intent, REQUEST_ACTIVITY_FILE_LIST);

                    }
                });
                alertDialog2.setNegativeButton("Back",null);
                AlertDialog alert2 = alertDialog2.create();
                alert2.show();
                return true;

            case R.id.files_d:
                return true;

            case R.id.sync_cloud2mobile:
                String urls2[] = new String[1];
                urls2[0] = Config._listURL;
                filesOnCloud = getFilesOnCloud(RunActivity.this, urls2);
                String fileURL[] = new String[filesOnCloud.length];

                for(int i=0;i<fileURL.length;i++) fileURL[i] = "http://180.69.217.73:8080/OneOOMT/filedown.jsp?name="
                        + filesOnCloud[i];
                HttpDownloadUtility.downloadFileAsync(RunActivity.this,fileURL,mediaStorageDir.getAbsolutePath());
                return true;

            case R.id.clear_useless_mobile_activity:
                return true;

            case R.id.map:
                Intent intent = new Intent(RunActivity.this, FileActivity.class);
                intent.putExtra("locations",mList);
                startActivity(intent);
                return true;

            case R.id.lowbattery:
                mode_low_battery = ! mode_low_battery;
                doMyTimeTask(mode_low_battery?10000:1000);
                Toast.makeText(RunActivity.this,mode_low_battery?"Timer period - 10sec":"Timer period - 1sec", Toast.LENGTH_LONG).show();
                return true;

            case R.id.directdbupdate:
                direct_db_update = ! direct_db_update;
                Toast.makeText(RunActivity.this, direct_db_update?"Direct DB update mode":"No DB update", Toast.LENGTH_LONG).show();
                return true;

            case R.id.notify:
                return true;

            case R.id.weather:
                if(mList.size()>0) {
                    getMyWeather();
                    while(cur_myweather == null) {}
                    Toast.makeText(RunActivity.this, cur_myweather.toString(), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.memory:
                return true;


            case R.id.web:
                Intent wintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://180.69.217.73:8080/OneOOMT"));
                startActivity(wintent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    private String[] getFilesOnCloud(final Context context, String url[]) {
        new AsyncTask<String,Void,Boolean>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Boolean doInBackground(String... url) {
                JSONObject jObj = JSONUtil.getJSONFromUrl(url[0]);
                try {
                    JSONArray arr = (JSONArray)jObj.get("files");
                    filesOnCloud = new String[arr.length()];
                    filesizeOnCloud = new Long[arr.length()];

                    for(int i=0;i<arr.length();i++) {
                        JSONObject j = (JSONObject)arr.get(i);
                        filesOnCloud[i] = (String)j.get("name");
                        filesizeOnCloud[i] = Long.parseLong((String)j.get("size"));
                    }
                }catch(Exception e) {
                    Log.e(TAG,e.toString());
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                filesOnCloud = null;
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(context, "list download " + result + "!!", Toast.LENGTH_SHORT).show();
            }

        }.execute(url);

        while(filesOnCloud==null) {
            try {
                Log.e(TAG, "waiting for get filesOnCloud....");
                Thread.sleep(100); //0.1초 기다림
            }catch(Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        for(int i=0;i<filesOnCloud.length;i++) {
            Log.e(TAG, "File:" + filesOnCloud[i]);
        }

        return filesOnCloud;
    }


    private void getMyWeather() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                double lat = (double) mList.get(mList.size() - 1).latitude;
                double lon = (double) mList.get(mList.size() - 1).longitude;

                WeatherAPI weatherAPI = new WeatherAPI();
                WeatherAPI.myWeather myweather = weatherAPI.getMyWeather(lat, lon);

                if(myweather != null) Log.e(TAG, myweather.toString());
                else Log.e(TAG, "ERR] cannot get the weather information("+lat+","+lon+")");

                cur_myweather = myweather;
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }

        }.execute();
    }


    private void getWeatherInfo() {
        if(mList.size()==0) return;
        int lat = (int) mList.get(mList.size()-1).latitude;
        int lon = (int) mList.get(mList.size()-1).longitude;
        WeatherAPI weatherAPI = new WeatherAPI();
        String msg = null;

        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                double lat = (double) mList.get(mList.size() - 1).latitude;
                double lon = (double) mList.get(mList.size() - 1).longitude;

                WeatherAPI weatherAPI = new WeatherAPI();
                WeatherAPI.Weather weather = weatherAPI.getWeather(lat, lon);

                String mymsg = weather.getCity() + "(" + weather.getLat() + "," + weather.getIon() + ") Temp:"
                        + weather.getTemprature() + " Cloudy:" + weather.getCloudy();

                Log.e(TAG, mymsg);
                cur_weather = weather;
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }

        }.execute();
    }

    private void doHttpFileUploadAll(final Context context, String url) {
        if(url==null) url = "http://180.69.217.73:8080/OneOOMT/upload";
        final String _serverUrl = url;
        // Pop Up a Dialog

        new AsyncTask<Void,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);
            final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
            HttpURLConnection urlConnection = null;
            String attachmentName = null;
            String attachmentFileName = null;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {

                File flist[] = MyActivityUtil.getFiles();
                asyncDialog.setMax(flist.length);

                for (int i = 0; i < flist.length; i++) {

                    File file = flist[i];
                    attachmentName = attachmentFileName = flist[i].getName();

                    try {
                        URL serverUrl = new URL(_serverUrl);
                        urlConnection = (HttpURLConnection) serverUrl.openConnection();

                        // request 준비
                        HttpURLConnection httpUrlConnection = null;
                        URL url = new URL(_serverUrl);
                        httpUrlConnection = (HttpURLConnection) url.openConnection();
                        httpUrlConnection.setUseCaches(false);
                        httpUrlConnection.setDoOutput(true);

                        httpUrlConnection.setRequestMethod("POST");
                        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                        httpUrlConnection.setRequestProperty(
                                "Content-Type", "multipart/form-data;boundary=" + this.boundary);

                        // content wrapper시작
                        DataOutputStream request = new DataOutputStream(
                                httpUrlConnection.getOutputStream());

                        request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                        request.writeBytes("Content-Disposition: form-data; name=\"" +
                                this.attachmentName + "\";filename=\"" +
                                this.attachmentFileName + "\"" + this.crlf);
                        request.writeBytes(this.crlf);

                        OutputStream out = httpUrlConnection.getOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int readcount = 0;
                        while ((readcount = fis.read(buffer)) != -1) {
                            //Log.e(TAG, "readcount:" + readcount);
                            out.write(buffer, 0, readcount);
                        }
                        out.flush();

                        request.writeBytes(this.crlf);
                        request.writeBytes(this.twoHyphens + this.boundary +
                                this.twoHyphens + this.crlf);

                        request.flush();
                        request.close();

                        Log.e(TAG,"end of write to web server");

                        //==============받기===============
                        InputStream is = httpUrlConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sbResult = new StringBuffer();
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            //Log.e(TAG, "RESPONSE:" + str);
                            sbResult.append(str);
                        }

                        asyncDialog.setProgress(i);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }

                }
                return null;
            }


            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Uploading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Uploading success", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void doHttpFileUpload3(final Context context, final String fname) {

        new AsyncTask<Void,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);

            final String serverUrl = "http://180.69.217.73:8080/OneOOMT/upload";
            final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
            // 기타 필요한 내용
            String attachmentName = fname;
            String attachmentFileName = fname;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";
            long filelength = 0;

            public void go() throws Exception {

                //==============환경===============
                File file = new File(mediaStorageDir, fname);
                filelength = file.length();
                asyncDialog.setMax(100);

                Log.e(TAG, "filename to uplad: " + file.getAbsolutePath());


                // request 준비
                HttpURLConnection httpUrlConnection = null;
                URL url = new URL(serverUrl);
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);

                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + this.boundary);

                // content wrapper시작
                DataOutputStream request = new DataOutputStream(
                        httpUrlConnection.getOutputStream());

                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        this.attachmentName + "\";filename=\"" +
                        this.attachmentFileName + "\"" + this.crlf);
                request.writeBytes(this.crlf);

                asyncDialog.setMax((int)(file.length() / 1000));

                OutputStream out = httpUrlConnection.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int readcount = 0;

                long readtot = 0;
                while ((readcount = fis.read(buffer)) != -1) {

                    //Log.e(TAG, "readcount:" + readcount);
                    out.write(buffer, 0, readcount);
                    readtot = readtot + readcount;
                    int percentage = (int)((float)(readtot / filelength) * 100f);
                    asyncDialog.setProgress(percentage);
                }
                out.flush();

                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary +
                        this.twoHyphens + this.crlf);

                request.flush();
                request.close();

                //Log.e(TAG,"end of write to web server");

                //==============받기===============
                InputStream is = httpUrlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sbResult = new StringBuffer();
                String str = "";
                while ((str = br.readLine()) != null) {
                    //Log.e(TAG, "RESPONSE:" + str);
                    sbResult.append(str);
                }
            }


            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    go();
                }catch(Exception e ){
                    Log.e(TAG, e.toString());
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Uploading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Activity("+fname+") Uploaded successfully...", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }
}


