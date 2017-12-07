package com.joonho.runme;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joonho.runme.util.ActivityStat;
import com.joonho.runme.util.ActivityUtil;
import com.joonho.runme.util.CalDistance;
import com.joonho.runme.util.MyActivity;
import com.joonho.runme.util.MyNotifier;
import com.joonho.runme.util.StringUtil;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "Main2Activity";
    private File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    String backupdir = StringUtil.DateToString1(new Date(), "yyyyMMdd");

    public  SharedPreferences mPref = null;
    public  SharedPreferences.Editor mEditor = null;

    private long start_time, end_time;
    private TextView tv_time_elapsed = null;
    private TextView tv_total_distance = null;
    private TextView tv_avg_pace = null;
    private TextView tv_cur_pace = null;
    private TextView tv_address = null;
    private TextView tv_lat_lng_altitude = null;
    private TextView tv_message = null;

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


    private double paces[] = new double[1000]; //upto 1000 km
    private long   startime_paces[] = new long[1000]; // upto 1000 start time

    private MyActivity start_loc;
    private ArrayList<MyActivity> mList = new ArrayList<MyActivity>();

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
        start = _start;
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

    public void loadSharedPreferences() {
        mPref = getSharedPreferences("setup", MODE_PRIVATE);
        isStarted = mPref.getBoolean("isStarted", true);
        total_distance = mPref.getFloat("total_distance", 0f);
        start = mPref.getLong("start", new Date().getTime());
        last_fname = mPref.getString("last_fname", null);
        lastkm = mPref.getInt("lastkm", 0);
        lastmin = mPref.getInt("lastmin", 0);
        lasthour = mPref.getInt("lasthour", 0);

        Log.e(TAG,"isStarted:" + isStarted);
        Log.e(TAG, "total_distance:" + total_distance);
        Log.e(TAG,"start:"+ new Date(start));
        Log.e(TAG,"last_fname:" + last_fname);
        Log.e(TAG,"lastkm:" + lastkm);
        Log.e(TAG,"lastmin:" + lastmin);
        Log.e(TAG,"lasthour:" + lasthour);
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
        setContentView(R.layout.activity_main2);

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
            tv_avg_pace = (TextView) findViewById(R.id.tv_avg_pace02);
            tv_cur_pace = (TextView) findViewById(R.id.tv_cur_pace);
            tv_address = (TextView) findViewById(R.id.tv_address);
            tv_lat_lng_altitude = (TextView) findViewById(R.id.tv_lat_lng_altitude);
            tv_message = (TextView) findViewById(R.id.tv_message);

            imb_stop_timer = (ImageButton) findViewById(R.id.imb_stop_timer);
            //doMyTimeTask(); 신규 활동이 아닌 과거 활동을 복원함.

            loadSharedPreferences();

            if(last_fname != null) {
                File lastFile = new File(mediaStorageDir, last_fname);
                mList = ActivityUtil.deserializeFile(lastFile);
                Log.e(TAG, "mList null -- Activities reloaded..... ");

                String msg = String.format(last_fname + "로부터 " + mList.size() + " 경로(약"+lastkm+"km)가 복윈되었습니다.");
                tv_message.setText(msg);
            }
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

        loadSharedPreferences();

        if(mList == null) {
            if(last_fname != null) {
                File lastFile = new File(mediaStorageDir, last_fname);
                mList = ActivityUtil.deserializeFile(lastFile);
                Log.e(TAG, "mList null -- Activities reloaded..... ");

                String msg = String.format(last_fname + "로부터 " + mList.size() + " 경로(약"+lastkm+"km)가 복윈되었습니다.");
                tv_message.setText(msg);
            }
        } else {
            if(mList.size()==0) {
                if(last_fname != null) {
                    File lastFile = new File(mediaStorageDir, last_fname);
                    mList = ActivityUtil.deserializeFile(lastFile);
                    Log.e(TAG, "mList size 0 -- Activities reloaded..... ");

                    String msg = String.format(last_fname + "로부터 " + mList.size() + " 경로(약"+lastkm+"km)가 복윈되었습니다.");
                    tv_message.setText(msg);
                }
            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.e(TAG,"------- onStop() called");
        saveSharedPreferences();
        super.onStop();
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
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String provider = location.getProvider();
                String msg = String.format("위치가 %s로부터 추가되어 경로의수는 %d입니다", provider, mList.size());
                tv_message.setText(msg);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };

        if ((ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(Main2Activity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{
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
        String locationProvider = null;
        // locationProvider = LocationManager.NETWORK_PROVIDER;
        if(!noGPS) locationProvider = LocationManager.GPS_PROVIDER;

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLatitude();
                double lat = lastKnownLocation.getLatitude();
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

    public String LocTimeStr(Location loc) {
        String added_on = StringUtil.DateToString1(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
        return added_on;
    }

    public long Str2LocTime(String str) {
        Date date = StringUtil.StringToDate(str,"yyyy년MM월dd일_HH시mm분ss초");
        return date.getTime();
    }

    public void doMyTimeTask() {
        mTask =new Main2Activity.MyTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 1000, 1000);  // 10초

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

            Location sl = getLocation();
            if(sl != null) {
                start_loc = new MyActivity(sl.getLatitude(), sl.getLongitude(), sl.getAltitude(), LocTimeStr(sl));
                mList.add(start_loc);

                String caddr = getCurAddress(getApplicationContext(),sl);
                tv_address.setText(caddr);
                String lla = String.format("위도:%3.3f, 경도:%3.3f, 고도:%3.1f", sl.getLatitude(), sl.getLongitude(), sl.getAltitude());
                tv_lat_lng_altitude.setText(lla);
                String provider = sl.getProvider();
                String msg = String.format("위치가 %s로부터 추가되어 경로의수는 %d입니다", provider, mList.size());
                tv_message.setText(msg);
            }
        }
    }

    public void alertDialogChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
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
                String fname = ActivityUtil.serializeWithCurrentTime(mList);
                Toast.makeText(Main2Activity.this, "" + fname + " saved ...", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.setNegativeButton("NEW", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String fname = ActivityUtil.serializeWithCurrentTime(mList);
                Toast.makeText(Main2Activity.this, "" + fname + " created and started new activity...", Toast.LENGTH_LONG).show();

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

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_message:
            case R.id.imb_stop_timer:
                alertDialogChoice();
                break;
        }
    }

    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            if(!isStarted) return;
            //start = System.currentTimeMillis(); 시작시간은 onCreate or App실행시 초기화됨.

            Main2Activity.this.runOnUiThread(new Runnable() {

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
                    if (cur_loc == null) {

                    } else {
                        if (mList.size() == 0) {
                            MyActivity ma = new MyActivity(cur_loc.getLatitude(), cur_loc.getLongitude(), cur_loc.getAltitude(), LocTimeStr(cur_loc));
                            mList.add(ma);

                            String cur_addr = getCurAddress(Main2Activity.this, cur_loc);
                            tv_address.setText(cur_addr);
                            total_distance = 0;
                            String lla = String.format("위도:%3.3f, 경도:%3.3f, 고도:%3.1f", cur_loc.getLatitude(), cur_loc.getLongitude(), cur_loc.getAltitude());
                            tv_lat_lng_altitude.setText(lla);
                        } else {
                            MyActivity last_loc = mList.get(mList.size() - 1);
                            CalDistance cd = new CalDistance(last_loc.latitude, last_loc.longitude, cur_loc.getLatitude(), cur_loc.getLongitude());
                            if (!Double.isNaN(cd.getDistance())) {
                                cur_dist = cd.getDistance();
                                cur_elapsed_time = cur_loc.getTime() - Str2LocTime(last_loc.added_on);

                                if (cur_dist < 1f) {
                                    // skip location information of 1m
//                                    String provider = cur_loc.getProvider();
//                                    String msg = String.format("%s로부터 추가된 위치는 거리가 %3.1fM로 제외...", provider, cur_dist);
//                                    tv_message.setText(msg);
                                } else {
                                    total_distance = total_distance + cur_dist;
                                    MyActivity ma = new MyActivity(cur_loc.getLatitude(), cur_loc.getLongitude(), cur_loc.getAltitude(), LocTimeStr(cur_loc));
                                    mList.add(ma);

                                    String cur_addr = getCurAddress(Main2Activity.this, cur_loc);
                                    tv_address.setText(cur_addr);
                                    String lla = String.format("위도:%3.1f, 경도:%3.1f, 고도:%3.1f", cur_loc.getLatitude(), cur_loc.getLongitude(), cur_loc.getAltitude());
                                    tv_lat_lng_altitude.setText(lla);

                                    String provider = cur_loc.getProvider();
                                    String msg = String.format("위치가 %s로부터 추가되어 경로의수는 %d입니다", provider, mList.size());
                                    tv_message.setText(msg);
                                }
                            }
                        }

                        double dist_kilo = total_distance / 1000f;

//                        if ((int) dist_kilo > lastkm) {
//                            last_fname = ActivityUtil.serializeWithCurrentTime(mList);
//                            doHttpFileUpload3(Main2Activity.this, last_fname);
//                            lastkm++;
//
//                            String alertmsg = "" + lastkm + " km를 활동하였습니다. \n " + last_fname + " 업데이트되었습니다.";
//                            MyNotifier.go(Main2Activity.this, "100대명산거리알람", alertmsg);
//                        }


                        String distance_str = String.format("%.2f", dist_kilo);
                        tv_total_distance.setText(distance_str);

                        double elapsed_time_sec = (double) (elapsed_time / 1000l);
                        double km_per_sec = (double) (elapsed_time_sec / dist_kilo);
                        String avg_pace = String.format("%2d:%02d", (int) (km_per_sec / 60), (int) (km_per_sec % 60));
                        if (dist_kilo != 0) tv_avg_pace.setText(avg_pace);

//                        if ((int) (elapsed_time_sec / 3600) > lasthour) { //1시간 업데이트
//                            // 웹서버 업로드 하는것으로 향후 구현하기로 함
//                            lasthour++;
//                            last_fname = ActivityUtil.serializeWithCurrentTime(mList);
//                            doHttpFileUpload3(Main2Activity.this, last_fname);
//
//                            String alertmsg = "" + lasthour + " 시간을 활동하였습니다.\n " + last_fname + " 업로드하였습니다.";
//                            MyNotifier.go(Main2Activity.this, "100대명산시간알람", alertmsg);
//
//                        } else if ((int) (elapsed_time_sec / 600) > lastmin) { //10분 알람
//                            last_fname = ActivityUtil.serializeWithCurrentTime(mList);
//                            doHttpFileUpload3(Main2Activity.this, last_fname);
//                            lastmin = lastmin + 10;
//                            String alertmsg = "" + lastmin + " 분을 활동하였습니다. " + last_fname + " 업데이트하였습니다.";
//                            MyNotifier.go(Main2Activity.this, "100대명산10분알람", alertmsg);
//                        }

                        double cur_dist_kilo = cur_dist / 1000f;
                        double cur_elapsed_time_sec = (double) (cur_elapsed_time / 1000l);
                        double cur_km_per_sec = (double) (cur_elapsed_time_sec / cur_dist_kilo);
                        String cur_pace = String.format("%2d:%02d", (int) (cur_km_per_sec / 60), (int) (cur_km_per_sec % 60));
                        if (cur_dist_kilo != 0) tv_cur_pace.setText(cur_pace);
                    }
                }
            });
        }
    }

    public void alertUploadServerChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main2Activity.this);
        alertDialog.setTitle("Choose Upload Servlet");

        final EditText ed = new EditText(alertDialog.getContext());
        ed.setText("http://180.69.217.73:8080/OneOOMT/upload");
        alertDialog.setView(ed);

        alertDialog.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String url = ed.getText().toString();
                doHttpFileUploadAll(Main2Activity.this, url);
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
            case R.id.web:
                Intent wintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://180.69.217.73:8080/OneOOMT"));
                startActivity(wintent);
                return true;

            case R.id.webupload:
                last_fname = ActivityUtil.serializeWithCurrentTime(mList);
                doHttpFileUpload3(Main2Activity.this, last_fname);
                return true;

            case R.id.uploadall2:
                alertUploadServerChoice();
                return true;

            case R.id.uploadall:
                doHttpFileUploadAll(Main2Activity.this, null);
                return true;

            case R.id.map:
                Intent intent = new Intent(Main2Activity.this, CurActivity.class);
                intent.putExtra("locations",mList);

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
                        ActivityUtil.showActivityAlertDialog(Main2Activity.this, afile, index);
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
                        Intent intent = new Intent(Main2Activity.this, ActFileActivity.class);
                        intent.putExtra("file", afile.getAbsolutePath());
                        intent.putExtra("pos", index);
                        startActivityForResult(intent, REQUEST_ACTIVITY_FILE_LIST);

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
                MyNotifier.go(Main2Activity.this, "Hello", "World");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode,resultCode,intent);
        switch(requestCode) {
            case REQUEST_ACTIVITY_FILE_LIST:
                String tmpFname = (String)intent.getStringExtra("fname");
                if(tmpFname==null) return;

                ArrayList<MyActivity> tmpActList =(ArrayList<MyActivity>)intent.getSerializableExtra("locations");
                ActivityStat as = ActivityUtil.getActivityStat(tmpActList);

                long elapsed = new Date().getTime() - as.start.getTime();
                double elpased_min = (double)(elapsed / 60f);
                int elapsed_hour = (int)(elpased_min/3600f);
                int elapsed_min = (int)((elpased_min - elapsed_hour*3600f) /60f);

                initSharedPreferences(tmpActList,
                        as.distanceM,
                        true,
                        as.start.getTime(),
                        tmpFname,
                        (int)as.distanceKm,
                        elapsed_hour,
                        elapsed_min
                );
                Toast.makeText(Main2Activity.this,"Activity Reloaded ..... ", Toast.LENGTH_LONG).show();
        }
    }

    private void doHttpFileUploadAll(final Context context, String url) {
        if(url==null) url = "http://180.69.217.73:9090/OneOOMT/upload";
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

                File flist[] = ActivityUtil.getFiles();
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

                Log.e(TAG,"end of write to web server");

                //==============받기===============
                InputStream is = httpUrlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sbResult = new StringBuffer();
                String str = "";
                while ((str = br.readLine()) != null) {
                    Log.e(TAG, "RESPONSE:" + str);
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


