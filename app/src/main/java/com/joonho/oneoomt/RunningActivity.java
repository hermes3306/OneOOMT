package com.joonho.oneoomt;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.stream.IntStream;

import com.joonho.oneoomt.LocalLocationService.LocalBinder;
import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.db.PropsDB;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.file.myPicture;
import com.joonho.oneoomt.util.CalBearing;
import com.joonho.oneoomt.util.CalDistance;
import com.joonho.oneoomt.util.PhotoUtil;
import com.joonho.oneoomt.util.modifiedDate;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.joonho.oneoomt.R.id.alertTitle;
import static com.joonho.oneoomt.R.id.map;
import static com.joonho.oneoomt.R.id.src_in;
import static com.joonho.oneoomt.R.id.tv_bearingmode;

public class RunningActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "RunningActivity";
    private GoogleMap mMap;
    private static float myzoom=16.0f;
    private static LocalLocationService mService;
    private static boolean mBound=false;
    private static boolean isOnStartCalled=false;
    private static double mMaxAlt = 0;
    private static Location mLastLoc=null;
    private static boolean mDrivingMode=true;
    private static Location mCurLoc=null;
    private static double mBearing=0;
    private static PolylineOptions mPlops = null;
    private static Polyline myLine = null;


    private static List<LatLng> mLatLngList = new ArrayList<LatLng>();
    private static Vector mLocTime = new Vector();
    private static List<myPicture> mMyPicture = new ArrayList<myPicture>();
    private static List<Marker> mMarkerList = new ArrayList<Marker>();

    /* SharedPreference */
    public static SharedPreferences mPref = null;
    public static SharedPreferences.Editor mEditor = null;

    public static int       pMarkerInterval;
    public static boolean   pdrawMarker;
    public static boolean   pdrawTrack;
    public static int       pTrackColor;
    public static int       pTrackWidth;
    public static boolean   pdrawGroudNum;
    public static boolean   pdirectDBUpdate;
    public static boolean   ponStartDBLoad;
    public static boolean   pshowPictures;
    public static int       pTimerPeriod;
    public static String    pLatestFilename;
    public static boolean   pIsStarted;
    public static long      pTime_start;
    public static int       pStartPos;

    final String colors[] = {"Red","Blue","White","Yellow","Black", "Green","Purple","Orange","Grey", "DarkGray", "Cyan"};
    final int track_color[] = {Color.RED, Color.BLUE, Color.WHITE, Color.YELLOW, Color.BLACK, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.GRAY, Color.DKGRAY, Color.CYAN};

    private void getSharedPreferences() {
        pMarkerInterval = mPref.getInt      ("pMarkerInterval",1000);
        pdrawMarker     = mPref.getBoolean  ("pdrawMarker", false);
        pdrawTrack      = mPref.getBoolean  ("pdrawTrack",  true);
        pTrackColor     = mPref.getInt      ("pTrackColor", 0);
        pTrackWidth     = mPref.getInt      ("pTrackWidth", 25);
        pdrawGroudNum   = mPref.getBoolean  ("pdrawGroudNum", false);
        pdirectDBUpdate = mPref.getBoolean  ("pdirectDBUpdate", true);
        ponStartDBLoad  = mPref.getBoolean  ("ponStartDBLoad", true);
        pshowPictures   = mPref.getBoolean  ("pshowPictures", false);
        pTimerPeriod    = mPref.getInt      ("pTimerPeriod", 1000);
        pLatestFilename = mPref.getString   ("pLatestFilename","Last_Activity");

        // Readonly Property
        pIsStarted      = mPref.getBoolean  ("pIsStarted", false);
        pTime_start     = mPref.getLong     ("pTime_start", 0l);
        pStartPos       = mPref.getInt      ("pStartPos", 0);
    }

    private void setSharedPrefrences() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunningActivity.this);
        alertDialog.setTitle("Property Setting");


        final EditText      et_pMarkerInterval = new EditText(RunningActivity.this);
        final ToggleButton  tg_pdrawMarker = new ToggleButton(RunningActivity.this);
        final ToggleButton  tg_pdrawTrack = new ToggleButton(RunningActivity.this);
        final Spinner       sp_pTrackColor = new Spinner(RunningActivity.this);
        final EditText      et_pTrackWidth = new EditText(RunningActivity.this);
        final ToggleButton  tg_pdrawGroudNum = new ToggleButton(RunningActivity.this);
        final ToggleButton  tg_pdirectDBUpdate = new ToggleButton(RunningActivity.this);
        final ToggleButton  tg_ponStartDBLoad = new ToggleButton(RunningActivity.this);
        final ToggleButton  tg_pshowPictures = new ToggleButton(RunningActivity.this);
        final EditText      et_pTimerPeriod = new EditText(RunningActivity.this);
        final EditText      et_pLatestFilename = new EditText(RunningActivity.this);

        getSharedPreferences();
        et_pMarkerInterval.setText(""+pMarkerInterval);
        tg_pdrawMarker.setChecked(pdrawMarker);
        tg_pdrawTrack.setChecked(pdrawTrack);
        et_pTrackWidth.setText(""+pTrackWidth);
        tg_pdrawGroudNum.setChecked(pdrawGroudNum);
        tg_pdirectDBUpdate.setChecked(pdirectDBUpdate);
        tg_ponStartDBLoad.setChecked(ponStartDBLoad);
        tg_pshowPictures.setChecked(pshowPictures);
        et_pTimerPeriod.setText(""+pTimerPeriod);
        et_pLatestFilename.setText(""+pLatestFilename);


        final LinearLayout ll_ver  = new LinearLayout(RunningActivity.this);
        ll_ver.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout ll_hor1 = new LinearLayout(RunningActivity.this);

        ll_hor1.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv1 = new TextView(RunningActivity.this); tv1.setText("MarkerInterval");
        ll_hor1.addView(tv1);
        ll_hor1.addView(et_pMarkerInterval);
        ll_ver.addView(ll_hor1);

        final LinearLayout ll_hor2 = new LinearLayout(RunningActivity.this);
        ll_hor2.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv2 = new TextView(RunningActivity.this); tv2.setText("drawMarker");
        ll_hor2.addView(tv2);
        ll_hor2.addView(tg_pdrawMarker);
        ll_ver.addView(ll_hor2);

        final LinearLayout ll_hor3 = new LinearLayout(RunningActivity.this);
        ll_hor3.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv3 = new TextView(RunningActivity.this); tv3.setText("drawTrack");
        ll_hor3.addView(tv3);
        ll_hor3.addView(tg_pdrawTrack);
        ll_ver.addView(ll_hor3);

        final LinearLayout ll_hor4 = new LinearLayout(RunningActivity.this);
        ll_hor4.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv4 = new TextView(RunningActivity.this); tv4.setText("TrackColor");
        ll_hor4.addView(tv4);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_pTrackColor.setAdapter(spinnerArrayAdapter);
        sp_pTrackColor.setSelection(pTrackColor, true);

        Log.e(TAG," *** SPINNER2 *** : " + pTrackColor);

        ll_hor4.addView(sp_pTrackColor);
        ll_ver.addView(ll_hor4);


        final LinearLayout ll_hor5 = new LinearLayout(RunningActivity.this);
        ll_hor5.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv5 = new TextView(RunningActivity.this); tv5.setText("TrackWidth");
        ll_hor5.addView(tv5);
        ll_hor5.addView(et_pTrackWidth);
        ll_ver.addView(ll_hor5);

        final LinearLayout ll_hor6 = new LinearLayout(RunningActivity.this);
        ll_hor6.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv6 = new TextView(RunningActivity.this); tv6.setText("drawGroudNum");
        ll_hor6.addView(tv6);
        ll_hor6.addView(tg_pdrawGroudNum);
        ll_ver.addView(ll_hor6);

        final LinearLayout ll_hor7 = new LinearLayout(RunningActivity.this);
        ll_hor7.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv7 = new TextView(RunningActivity.this); tv7.setText("directDBUpdate");
        ll_hor7.addView(tv7);
        ll_hor7.addView(tg_pdirectDBUpdate);
        tg_pdirectDBUpdate.setTextOn("On"); tg_pdirectDBUpdate.setTextOff("Off");
        ll_ver.addView(ll_hor7);

        final LinearLayout ll_hor8 = new LinearLayout(RunningActivity.this);
        ll_hor8.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv8 = new TextView(RunningActivity.this); tv8.setText("onStartDBLoad");
        ll_hor8.addView(tv8);
        ll_hor8.addView(tg_ponStartDBLoad);
        ll_ver.addView(ll_hor8);

        final LinearLayout ll_hor81 = new LinearLayout(RunningActivity.this);
        ll_hor81.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv81 = new TextView(RunningActivity.this); tv81.setText("showPictures");
        ll_hor81.addView(tv81);
        ll_hor81.addView(tg_pshowPictures);
        ll_ver.addView(ll_hor81);

        final LinearLayout ll_hor9 = new LinearLayout(RunningActivity.this);
        ll_hor9.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv9 = new TextView(RunningActivity.this); tv9.setText("TimerPeriod");
        ll_hor9.addView(tv9);
        ll_hor9.addView(et_pTimerPeriod);
        ll_ver.addView(ll_hor9);


        final LinearLayout ll_hor10 = new LinearLayout(RunningActivity.this);
        ll_hor10.setOrientation(LinearLayout.HORIZONTAL);
        final TextView tv10 = new TextView(RunningActivity.this); tv10.setText("LatestFilename");
        ll_hor10.addView(tv10);
        ll_hor10.addView(et_pLatestFilename);
        ll_ver.addView(ll_hor10);

        alertDialog.setView(ll_ver);

        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                mEditor.putInt("pMarkerInterval", Integer.parseInt(et_pMarkerInterval.getText().toString()) );
                mEditor.putBoolean("pdrawMarker", tg_pdrawMarker.isChecked());

                mEditor.putBoolean("pdrawTrack",  tg_pdrawTrack.isChecked());
                mEditor.putInt("pTrackColor",        sp_pTrackColor.getSelectedItemPosition());
                mEditor.putInt("pTrackWidth", Integer.parseInt(et_pTrackWidth.getText().toString()));
                mEditor.putBoolean("pdrawGroudNum",  tg_pdrawGroudNum.isChecked());
                mEditor.putBoolean("pdirectDBUpdate", tg_pdirectDBUpdate.isChecked());
                mEditor.putBoolean("ponStartDBLoad",  tg_ponStartDBLoad.isChecked());
                mEditor.putBoolean("pshowPictures",  tg_pshowPictures.isChecked());
                mEditor.putInt("pTimerPeriod", Integer.parseInt(et_pTimerPeriod.getText().toString()));
                mEditor.putString("pLatestFilename", et_pLatestFilename.getText().toString());
                mEditor.commit();
                getSharedPreferences();

            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick (DialogInterface dialog, int whichButton) {

            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        if (    (ContextCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunningActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(RunningActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(RunningActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        mPref = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mPref.edit();
        getSharedPreferences();


        mLatLngList = dbgateway.allLatLng(getApplicationContext());
        mLocTime = new Vector();for(int i=0;i<mLatLngList.size();i++) mLocTime.add(System.currentTimeMillis());

        btn_event();
        doMyTimeTask();
    }

    // 가로 회전 세로 회전
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
//        setContentView(R.layout.activity_running);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(map);
//        mapFragment.getMapAsync(this);

        mPref = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mPref.edit();
        getSharedPreferences();

        mLatLngList = dbgateway.allLatLng(getApplicationContext());
        mLocTime = new Vector();for(int i=0;i<mLatLngList.size();i++) mLocTime.add(System.currentTimeMillis());

        btn_event();
        doMyTimeTask();
    }


    @Override
    protected void onStart(){
        super.onStart();
        isOnStartCalled = true;
        Log.e(TAG,"onStart() BEGIN" + Now());
        // 이미 서비스를 바인딩 경우에는 다시 바인딩 하지 않는다.
        if(mConnection != null && mService != null) {
            Log.e(TAG,"****** Service Already Binded!!!!!!");
            Log.e(TAG,"****** LastLoc: " + mService.getLastLocation() + " **");
            return;
        }

        Intent intent = new Intent(RunningActivity.this, com.joonho.oneoomt.LocalLocationService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if(mService!= null)  mBound = true;

        //registerAlarm();
        Log.e(TAG,"onStart() END" + Now());
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mBound)  {
            try {
                unbindService(mConnection);
            }catch(Exception e) {
                Log.e(TAG, "Error while onStop.." + e.toString());
            } finally {
                mBound = false;
            }
        }
        Log.e(TAG,"onStop()");
    }

    private ServiceConnection mConnection=new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder=(LocalBinder)service;
            mService=binder.getService();
            if(mService != null){
                Log.i(TAG, "Service is bonded successfully!");
            }
        }
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            mBound = false;
            Log.e(TAG, "onServiceDisconnected()");
        }
    };

    // **  ----------------------------------------------------------------------------------------
    //        Background Timer Task Class Definition, 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **

    public void dashboard_speed(String speed) {
        //Log.e(TAG, ">>>>>>> dashboard_speed() >>>>>>");

        TextView tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_speed.setVisibility(VISIBLE);
        tv_speed.setTextColor(Color.DKGRAY);
        tv_speed.setTextSize(25);
        tv_speed.setText(speed);
    }

    public void dashboard_bearing(LatLng ll1, LatLng ll2) {
        if(ll1 == null || ll2 == null) return;
        CalBearing cb = new CalBearing(ll1.latitude, ll1.longitude, ll2.latitude, ll2.longitude);

        mBearing = cb.getBearing();

        TextView tv_bearing = (TextView) findViewById(R.id.tv_bearing);
        tv_bearing.setText("Ang:" + cb.getBearing() + "");

        final TextView tv_bearingmode = (TextView) findViewById(R.id.tv_bearingmode);
        tv_bearingmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrivingMode = !mDrivingMode;
                if(mDrivingMode) {
                    tv_bearingmode.setText("Mode:Driving");
                }else {
                    tv_bearingmode.setText("Mode:Fixed");
                }
                show_cur_loc();
            }
        });
        if(mDrivingMode) {
            tv_bearingmode.setText("Mode:Driving");
        }else {
            tv_bearingmode.setText("Mode:Fixed");
        }
    }

    public void dashboard_distances(LatLng ll1, LatLng ll2, Location mLastLoc,  Location mCurLoc) {
        //if(ll1==null || ll2 == null || mLastLoc == null || mCurLoc == null) return;

        CalDistance cd1=null, cd2=null;
        if(ll1 != null && ll2 != null) {
            cd1 = new CalDistance(ll1.latitude,
                    ll1.longitude,
                    ll2.latitude,
                    ll2.longitude);
            TextView tv_d1 = (TextView) findViewById(R.id.tv_what);
            tv_d1.setText("" + cd1.getDistance());
        }

        if(mLastLoc != null && mCurLoc != null) {
            cd2 = new CalDistance(mLastLoc.getLatitude(),
                    mLastLoc.getLongitude(),
                    mCurLoc.getLatitude(),
                    mCurLoc.getLongitude());
            TextView tv_d2 = (TextView) findViewById(R.id.tv_who);
            tv_d2.setText(""+cd2.getDistance());
        }
    }

    public static int last_pic_loc01=-1;
    public static int last_pic_loc02=-1;
    public static int last_pic_loc03=-1;

    public static int last_pic_loc[] = new int[100];
    static {
        for(int i=0;i<last_pic_loc.length;i++) {
            last_pic_loc[i] = -1;
        }
    }

    public void dashboard_3pics() {
        // 현재 위치 서비스로 다시 가져 온후 진행함.
        mCurLoc = mService.getLastLocation();
        if(mCurLoc==null) {
            return;
        }

        ImageView imv_pic1 = (ImageView) findViewById(R.id.imv_pic1);
        ImageView imv_pic2 = (ImageView) findViewById(R.id.imv_pic2);
        ImageView imv_pic3 = (ImageView) findViewById(R.id.imv_pic3);

        double minDist=Double.MAX_VALUE;
        int mPos=0;
        int msize = PhotoUtil.myPictureList.size();

        for(int i=0;i<last_pic_loc.length;i++) {
            Log.e(TAG, "" + i + ":" + last_pic_loc[i]);
            if(last_pic_loc[i] == -1) break;
        }

        for(int i=0;i<msize;i++) {
            boolean contains = false;
            for(int j=0;j<last_pic_loc.length;j++) {
                if(last_pic_loc[j] == -1 )  break;
                if(last_pic_loc[j] ==  i )  {
                    contains = true;
                }
            }
            if(contains) continue;

            myPicture mp = PhotoUtil.myPictureList.get(i);
            CalDistance cd = new CalDistance(mp.myactivity.latitude, mp.myactivity.longitude, mCurLoc.getLatitude(), mCurLoc.getLongitude());
            if(minDist > cd.getDistance()) {
                minDist = cd.getDistance();
                mPos = i;
            }
        }

        if(last_pic_loc[0] == mPos) {
            for(int i=0;i<last_pic_loc.length;i++) last_pic_loc[i]=-1;
        }

        //Toast.makeText(RunningActivity.this, "" + mPos + "]" + minDist + "meters", Toast.LENGTH_LONG ).show();
        for(int i=last_pic_loc.length-1; i>0;i--) {
            last_pic_loc[i] = last_pic_loc[i-1];
        }
        last_pic_loc[0] = mPos;

        if(last_pic_loc[0]  != -1) {
            Bitmap capturebmp01 = getPreview(PhotoUtil.myPictureList.get(last_pic_loc[0] ).filepath);
            imv_pic1.setImageBitmap(capturebmp01);
            imv_pic1.setRotation(90);
        }
        if(last_pic_loc[1] != -1) {
            Bitmap capturebmp02 = getPreview(PhotoUtil.myPictureList.get(last_pic_loc[1]).filepath);
            imv_pic2.setImageBitmap(capturebmp02);
            imv_pic2.setRotation(90);
        }
        if(last_pic_loc[2] != -1) {
            Bitmap capturebmp03 = getPreview(PhotoUtil.myPictureList.get(last_pic_loc[2]).filepath);
            imv_pic3.setImageBitmap(capturebmp03);
            imv_pic3.setRotation(90);
        }
    }

    Bitmap getPreview(String filepath) {
        File image = new File(filepath);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / 120; //120 = Thumnail Size
        return BitmapFactory.decodeFile(image.getPath(), opts);
    }



    public void dashboard_time_dist_speed() {
        TextView tv_time = (TextView) findViewById(R.id.tv_time);
        TextView tv_dist = (TextView) findViewById(R.id.tv_dist);
        TextView tv_avgspeed = (TextView) findViewById(R.id.tv_avgspd);
        TextView tv_alt = (TextView) findViewById(R.id.tv_alt);
        final ImageButton bt_start  = (ImageButton)findViewById(R.id.imgbt_start);
        final ImageButton bt_stop = (ImageButton)findViewById(R.id.imgbt_stop);
        final LinearLayout llo_left_top = (LinearLayout)findViewById(R.id.llo_left_top);

        if(!pIsStarted) {
            tv_time.setVisibility(INVISIBLE);
            tv_dist.setVisibility(INVISIBLE);
            bt_stop.setVisibility(INVISIBLE);
            bt_start.setVisibility(View.VISIBLE);
            llo_left_top.setVisibility(View.INVISIBLE);
            return;
        } else {
            tv_time.setTextColor(Color.BLUE);
            tv_dist.setTextColor(Color.RED);

            tv_time.setVisibility(VISIBLE);
            tv_dist.setVisibility(VISIBLE);
            bt_stop.setVisibility(VISIBLE);
            bt_start.setVisibility(View.INVISIBLE);
            llo_left_top.setVisibility(View.VISIBLE);
        }

        tv_time.setTextSize(25);
        tv_dist.setTextSize(25);

        loc_dist = distance_of_track(pStartPos, mLatLngList.size());

        float loc_distf = (float)loc_dist;
        String loc_dist_str = (loc_dist > 1000)? String.format("%.2fkm", loc_distf/1000f) : String.format("%dmeters", loc_dist);

        long time_rap = System.currentTimeMillis();
        long time_ela = time_rap - pTime_start;

        String e_str = "";
        int hnum = (int)(time_ela/1000/60/60);
        int mnum = (int)((time_ela - (hnum*60*60*1000))/1000/60);
        int snum = (int)((time_ela - (hnum*60*60*1000) - (mnum*60*1000)) /1000);

        if(hnum >0) e_str += "" + hnum +"h";
        if(mnum >0) e_str += "" + mnum + "m";
        if(snum >0) e_str += "" + snum + "s";

        tv_time.setText(e_str);
        tv_dist.setText(loc_dist_str);
        tv_avgspeed.setText("Avg:" +getavgspeed() + "km/h");

        String info = String.format("Alt:%.1f/%.1fM", mCurLoc.getAltitude(), mMaxAlt);
        tv_alt.setText(info);

        Log.e(TAG, "** running dashboard info >  " + info);
    }

    public static int run_status = 0;
    public static long run_max_altitude = 0;
    public static long time_rap=0, loc_points=0, loc_dist=0;
    public static String runningpannel_info = null;


    public String formatTime(long lTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lTime);
        return (c.get(Calendar.HOUR_OF_DAY) + "시 " + c.get(Calendar.MINUTE) + "분 " + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "초");
    }

    public String getavgspeed() {
        if(!pIsStarted) {
            return null;
        }

        if(pTime_start==0) {
            pTime_start = System.currentTimeMillis();
            mEditor.putLong("pTime_start",pTime_start);
            mEditor.commit();
        }
        time_rap = System.currentTimeMillis();

        loc_dist = distance_of_track(pStartPos,mLatLngList.size());
        float loc_distf = (float)loc_dist;
        double speed_m_per_sec   = loc_dist / ((time_rap - pTime_start)/1000.0f);
        double speed_km_per_sec  = speed_m_per_sec / 1000;
        double speed_km_per_hour = speed_km_per_sec * 3600;
        int speed_km_per_hourInt = (int)speed_km_per_hour;
        String dstr = String.format("%d", speed_km_per_hourInt);
        return dstr;
    }

    public static boolean loc_changed;
    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            long start = System.currentTimeMillis();
            RunningActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (mService == null) {
                        Toast.makeText(getApplicationContext(), "Location Service not Ready !!!!!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Location t_loc = mService.getLastLocation();
                    mLastLoc = mCurLoc;

                    loc_changed = false;

                    if (t_loc != null) {
                        if (t_loc.getLatitude() == 0 || t_loc.getLongitude() == 0) {
                        } else {
                            if (mLatLngList.size() > 0) {
                                mCurLoc = t_loc;
                                LatLng mlast = mLatLngList.get(mLatLngList.size() - 1);
                                LatLng CurLatLng = new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude());

                                if (!is_same_location(mlast, CurLatLng) || isOnStartCalled) {
                                    mLatLngList.add(new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude()));
                                    mLocTime.add(mCurLoc.getTime());

                                    loc_changed = true;
                                    isOnStartCalled = false;

                                    if(pdirectDBUpdate) {
                                        /****************************************************/
                                        dbgateway.addLoc(getApplicationContext(), mCurLoc);
                                        /****************************************************/
                                    }

                                    Log.e(TAG,"**** loc_changed Event Captured [?] .............");
                                }
                            } else { //list의 첫 데이타 입력시
                                mCurLoc = t_loc;
                                if (mCurLoc.getLatitude() != 0) {


                                    mLatLngList.add(new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude()));
                                    mLocTime.add(mCurLoc.getTime());

                                    if(pdirectDBUpdate) {
                                        /****************************************************/
                                        dbgateway.addLoc(getApplicationContext(), mCurLoc);
                                        /****************************************************/
                                    }

                                    loc_changed = true;
                                    isOnStartCalled = false;

                                    Log.e(TAG,"**** loc_changed Event Captured [0] .............");
                                }
                            }
                        }
                    }

                    if(loc_changed) {
                        if(mCurLoc != null) {
                            if(mLatLngList.size() < 2) return;

                            LatLng ll1 = mLatLngList.get(mLatLngList.size()-1) ;
                            LatLng ll2 = mLatLngList.get(mLatLngList.size()-2) ;

                            long t_pos = (long)mLocTime.get(mLatLngList.size()-1);
                            long t_pre = (long)mLocTime.get(mLatLngList.size()-2);

                            CalDistance cd = new CalDistance(ll1.latitude,
                                    ll1.longitude,
                                    ll2.latitude,
                                    ll2.longitude);

                            double speed_m_per_sec   = cd.getDistance() / ((t_pos - t_pre)/1000.0f);
                            double speed_km_per_sec  = speed_m_per_sec / 1000;
                            double speed_km_per_hour = speed_km_per_sec * 3600;
                            //int speed_km_per_hourInt = (int)speed_km_per_hour;

                            String dstr = String.format("%3.1f", speed_km_per_hour);
                            if (mMaxAlt < mCurLoc.getAltitude()) mMaxAlt = mCurLoc.getAltitude();

                            Log.e(TAG,"ll1=" + ll1);
                            Log.e(TAG,"ll2=" + ll2);
                            Log.e(TAG,"mLastLoc=" + mLastLoc);
                            Log.e(TAG,"mCurLoc=" + mCurLoc);

                            dashboard_speed(dstr);
                            dashboard_time_dist_speed();
                            dashboard_distances(ll1,ll2, mLastLoc, mCurLoc);
                            dashboard_bearing(ll1,ll2);

                            show_cur_loc();
                        }
                    } else{ // loc_change = false;
                            if(cnt_speed_zero==10) {
                                String dstr = String.format("%3d", 0);
                                dashboard_speed(dstr);
                                cnt_speed_zero=0;
                            } else cnt_speed_zero++;
                    }
                }
            });

        }
    }
    int cnt_speed_zero=0;

    // **  ----------------------------------------------------------------------------------------
    //        Utility , 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **

    private String Now() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
        String formatDate = sdfNow.format(date);
        return formatDate;
    }

    private boolean is_same_location(LatLng l1, LatLng l2) {
        if( (l1.latitude - l2.latitude) != 0)  return false;
        if( (l1.longitude - l2.longitude) !=0) return false;
        return true;
    }

    public void printAllLocs() {
        List<LatLng> lll = this.mLatLngList;
        if (lll == null) {
            Log.e(TAG, "List<LatLng> null !");
            return;
        }

        int i=0;
        for(LatLng ll : lll) {
            Log.e(TAG, "(" + i + ") Latitude/Longitude: " + ll.latitude + "," +ll.longitude);
            i++;
        }
    }

    public static String activity_file_name = null;
    // **  ----------------------------------------------------------------------------------------
    //        Button Event Registration , 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **
    private void btn_event() {
        // 001. Refresh Button
        ImageButton refresh = (ImageButton)findViewById(R.id.imgbt_refresh);
        refresh.setBackgroundColor(Color.TRANSPARENT);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    mMap.clear();
                    show_cur_loc();
            }}
        );

        // 002. Zoom In Buttons
        ImageButton zoomin = (ImageButton)findViewById(R.id.imgbt_zoomin);
        zoomin.setBackgroundColor(Color.TRANSPARENT);
        zoomin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myzoom = myzoom+1.0f;
                show_cur_loc();
            }}
        );

        // 003. Zoom Out Buttons
        ImageButton zoomout = (ImageButton)findViewById(R.id.imgbt_zoomout);
        zoomout.setBackgroundColor(Color.TRANSPARENT);
        zoomout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myzoom = myzoom-1.0f;
                show_cur_loc();
            }}
        );

        // 004. Home Buttons
        ImageButton homebt = (ImageButton)findViewById(R.id.imgbt_menu_popup);
        homebt.setBackgroundColor(Color.TRANSPARENT);
        homebt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        PopupMenu p = new PopupMenu(RunningActivity.this, v);
                        getMenuInflater().inflate(R.menu.mainoptmenu2, p.getMenu());
                        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                return onHomeOptionsItemSelected(item);
                            }
                        });
                        p.show();
                    }
        });

        // 005. Start/Stop Buttons
        final ImageButton bt_start  = (ImageButton)findViewById(R.id.imgbt_start);
        final ImageButton bt_stop = (ImageButton)findViewById(R.id.imgbt_stop);
        final LinearLayout llo_left_top = (LinearLayout)findViewById(R.id.llo_left_top);

        // 출발버튼
        bt_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!pIsStarted) {
                    pIsStarted=true;
                    mEditor.putBoolean("pIsStarted", pIsStarted);

                    pStartPos = mLatLngList.size()-1;
                    mEditor.putInt("pStartPos",pStartPos);
                    mEditor.commit();

                    Toast.makeText(getApplicationContext(),"Activity Started !!!",Toast.LENGTH_LONG).show();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunningActivity.this);
                    alertDialog.setTitle("Are you sure to reset all data?");
                    alertDialog.setMessage("All data will be reset if you want to click OK!");
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(activity_file_name!=null) {
                                dbgateway.serailizeActivitywithName(getApplicationContext(), activity_file_name + "_A.ser");
                            } else {
                                dbgateway.serailizeActivitywithName(getApplicationContext(), "LAST_ACTIVITY");
                            }
                            mLatLngList.clear();
                            mLocTime = new Vector();
                            mPlops = null;
                            mMap.clear();
                            pStartPos = 0;
                            mEditor.putInt("pStartPos",pStartPos);
                            mEditor.commit();

                            mMaxAlt = 0;
                            llo_left_top.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "All data on Screen is reset, if you want to confirm sync to DB else reboot to sync from DB",Toast.LENGTH_LONG );
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();

                    pTime_start = System.currentTimeMillis();
                    mEditor.putLong("pTime_start",pTime_start);
                    mEditor.commit();
                    loc_dist = 0;
                    loc_points = 0;

                    bt_start.setVisibility(INVISIBLE);
                    bt_stop.setVisibility(View.VISIBLE);

                }
            }}
        );

        /* 스톱 버튼 */
        bt_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunningActivity.this);
                alertDialog.setTitle("Activity Brief");
                String  info = runningpannel_info;
                alertDialog.setMessage(info);
                final EditText et = new EditText(RunningActivity.this);

                TextView tv_time = (TextView) findViewById(R.id.tv_time);
                TextView tv_dist = (TextView) findViewById(R.id.tv_dist);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
                Date now = new Date();
                String activity_name = "ACT_" + formatter.format(now) + "_" + tv_dist.getText() + "_" + tv_time.getText();
                et.setText(activity_name);
                alertDialog.setView(et);

                alertDialog.setPositiveButton("Save & Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(pIsStarted) {
                            pIsStarted=false;
                            mEditor.putBoolean("pIsStarted", pIsStarted);

                            pTime_start = 0;
                            mEditor.putLong("pTime_start",pTime_start);
                            mEditor.commit();


                            dbgateway.serailizeActivitywithName(getApplicationContext(), et.getText().toString() + ".ser");
                            Toast.makeText(getApplicationContext(),"Activity Saved & Quit !!!" + et.getText().toString(),Toast.LENGTH_LONG).show();
                            dashboard_time_dist_speed();
                            bt_stop.setVisibility(INVISIBLE);
                            bt_start.setVisibility(View.VISIBLE);
                            llo_left_top.setVisibility(INVISIBLE);
                        }
                    }
                });

                alertDialog.setNegativeButton("Save & Continue", new DialogInterface.OnClickListener(){
                    public void onClick (DialogInterface dialog, int whichButton) {
                            if(et.getText()!= null) dbgateway.serailizeActivitywithName(getApplicationContext(), et.getText().toString() + ".ser");
                            Toast.makeText(getApplicationContext(), "Activity Saved & Continue !!!" + et.getText().toString(),Toast.LENGTH_LONG).show();
                    }
                });

                Log.e(TAG,"Info" + info);
                AlertDialog alert = alertDialog.create();
                alert.show();
            }}
        );

        // 006. Take Picture / Magic / 3PIC
        final ImageButton bt_pic = (ImageButton)findViewById(R.id.imb_pic);
        bt_pic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }}
        );

        final ImageButton bt_magic = (ImageButton)findViewById(R.id.imb_magic);
        bt_magic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PhotoUtil pu = new PhotoUtil();
                pu.preview(RunningActivity.this, mCurLoc);
            }
        });

        final ImageButton bt_3pic = (ImageButton)findViewById(R.id.imb_3Pics);
        bt_3pic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dashboard_3pics();
            }
        });
    }

    // **  ----------------------------------------------------------------------------------------
    //        Background TimeTask 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **
    //** TimeTask start
    public void doMyTimeTask() {
        TimerTask mTask =new MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, 1000, pTimerPeriod); //delaytime(10sec), period(1sec)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mLatLngList.size() >0 ) {
            resumewithSavedValue();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngList.get(mLatLngList.size()-1), myzoom));
        } else {
            LatLng ll = new LatLng(29.51, 127.7);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, myzoom));
        }
        Log.e(TAG,">>>>>>>>>>>>>> onMapReady called !!!");
    }

    // **  ----------------------------------------------------------------------------------------
    //        Google Map Functions, 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **

    public static boolean moveCamerafirstcall=true;

    public void moveCamera() {
        if(mCurLoc==null) {
            Log.e(TAG,"mCurLoc == null");
            return;
        }
        LatLng curloc = new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude());

        myzoom = mMap.getCameraPosition().zoom;
        Log.e(TAG,"myzoom:" + myzoom);

        if(mDrivingMode) {
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(curloc)
                    .bearing((float)mBearing).zoom(myzoom).build();
            //        .bearing((float)mBearing).tilt(65.5f).zoom(18f).build();
            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(myzoom).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void addMarker() {
        if(mCurLoc==null) return;
        LatLng curloc = new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(curloc).title("Current Location..."));
    }

    public boolean isCurLocCatched() {
        if(mCurLoc==null) {
            Toast.makeText(getApplicationContext(),"Location not found yet!!!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(mCurLoc.getLatitude() == 0 || mCurLoc.getLongitude() ==0) {
            Toast.makeText(getApplicationContext(),"Location not found yet!!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void resumewithSavedValue() {
        final ImageButton bt_start  = (ImageButton)findViewById(R.id.imgbt_start);
        final ImageButton bt_stop = (ImageButton)findViewById(R.id.imgbt_stop);
        final LinearLayout llo_left_top = (LinearLayout)findViewById(R.id.llo_left_top);

        if(pIsStarted) {
            bt_stop.setVisibility(VISIBLE);
            bt_start.setVisibility(INVISIBLE);
            llo_left_top.setVisibility(VISIBLE);
        } else {
            bt_stop.setVisibility(INVISIBLE);
            bt_start.setVisibility(View.VISIBLE);
            llo_left_top.setVisibility(INVISIBLE);
        }
        show_cur_loc();
    }


    public static int show_cur_loc_cnt = 0;

    public void show_cur_loc()  {
        mMap.clear();

        long start = System.currentTimeMillis();
        if(pdrawTrack)  drawTrack();
        long cur = System.currentTimeMillis();

        Log.e(TAG,"Time (drawTrack) :" + (cur-start) + "ms");

        if(pdrawMarker || pdrawGroudNum) drawMarkersbyM(pMarkerInterval);
        long end = System.currentTimeMillis();

        Log.e(TAG, "Time (drawMarkersbyM) : " + (end-cur) + "ms");

        if(!pIsStarted) drawLastMarker();

        if(pshowPictures) showPicturesOnMap();

        if(pIsStarted) {
            drawRunningTrack();
        }

        moveCamera();
        show_cur_loc_cnt++;
    }

    private void drawRunningTrack() {
        drawStartingMarker();
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=pStartPos; i<mLatLngList.size();i++) {
            l.add(mLatLngList.get(i));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.CYAN);
        Polyline line = mMap.addPolyline(plo);
        line.setWidth(pTrackWidth+5);
        line.setPoints(l);

        drawRunTrackMarker();
        drawStopMarker();
    }



    // **  ----------------------------------------------------------------------------------------
    //        All the Menu management, 9/27/17, jhpark
    // **  -------------------------------------------------------------------------------------- **
    private boolean marker_enabled=true;
    private boolean track_enabled=true;
    private DBGateway dbgateway = new DBGateway();
    private PropsDB probs = new PropsDB();
    private int mtype = GoogleMap.MAP_TYPE_NORMAL;

    public boolean onHomeOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.main_opt_menu_item_save) {
            btn_save();
            return true;
        }

        if (id == R.id.main_opt_menu_item_hist) {
            dbgateway.getallActivities(RunningActivity.this);
            Intent i = new Intent(RunningActivity.this, HistoryActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.main_opt_menu_item_conf) {
            Intent i = new Intent(RunningActivity.this, PropsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.main_opt_menu_item_cam) {
            dispatchTakePictureIntent();
            return true;
        }

        if (id == R.id.main_opt_menu_item_marker) {
            if(marker_enabled) {
                mMap.clear();
                drawTrack();
                drawMarkersbyKm();
                marker_enabled=false;
            } else {
                mMap.clear();
                drawTrack();
                marker_enabled=true;
            }
            return true;
        }

        if (id == R.id.main_opt_menu_item_track) {
            if(track_enabled) {
                mMap.clear();
                drawTrack();
                drawMarkersbyKm();
                track_enabled=false;
            } else {
                mMap.clear();
                drawMarkersbyKm();
                track_enabled=true;
            }
            return true;
        }

        if (id == R.id.main_opt_menu_item_map) {
            if (mtype == GoogleMap.MAP_TYPE_NORMAL) mtype = GoogleMap.MAP_TYPE_SATELLITE;
            else mtype = GoogleMap.MAP_TYPE_NORMAL;
            mMap.setMapType(mtype);
            return true;
        }

        if (id == R.id.main_opt_menu_item_zoomin) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            return true;
        }

        if (id == R.id.main_opt_menu_item_zoomout) {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
            return true;
        }

        if (id == R.id.main_opt_menu_item_top_button_menu) {
            LinearLayout ll_upper_buttons = (LinearLayout)findViewById(R.id.ll_upper_buttons);
            if(ll_upper_buttons.getVisibility()==View.GONE) ll_upper_buttons.setVisibility(VISIBLE);
            else ll_upper_buttons.setVisibility(View.GONE);
            return true;
        }

        if (id == R.id.main_opt_menu_item_bottom_button_menu) {
            LinearLayout ll_bottom_buttons = (LinearLayout)findViewById(R.id.ll_bottom_buttons);
            if(ll_bottom_buttons.getVisibility()==View.GONE) ll_bottom_buttons.setVisibility(VISIBLE);
            else ll_bottom_buttons.setVisibility(View.GONE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btn_save() {
        String ret = dbgateway.serializeActivities(RunningActivity.this);
        if(ret==null) {
            Toast.makeText(getApplicationContext(),"Tracking Information Saving Failed!!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Activity Saved ("+ret+")!!!", Toast.LENGTH_SHORT).show();
        }

        String delete_when_save_opt = probs.getProperty(getApplicationContext(), "DELETE_WHEN_SAVE_ACT");
        Log.e(TAG, "****DELETE_WHEN_SAVE_ACT: " + delete_when_save_opt);

        if (delete_when_save_opt==null) return;

        if (delete_when_save_opt.equalsIgnoreCase("true")) {
            dbgateway.dbresetwithoutloastloc(RunningActivity.this);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Log.e(">>>>","before createImageFile");
                //photoFile = createImageFile();
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                //File storageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_.jpeg";
                photoFile = new File(storageDir, imageFileName);
                Log.e(">>>>","after createImageFile");
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.e(">>>>",ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.joonho.oneoomt.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                getApplicationContext().grantUriPermission(
                        "com.google.android.GoogleCamera",
                        photoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                startActivity(takePictureIntent);
            }
        }
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";


        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");

        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.e(TAG, "mCurrentPhotoPath:" + mCurrentPhotoPath);
        Toast.makeText(getApplicationContext(),"mCurrentPhotoPath:" + mCurrentPhotoPath, Toast.LENGTH_LONG).show();
        return image;
    }

    public void drawTrack() {

        if(mPlops == null) {
            mPlops = new PolylineOptions();
            myLine = mMap.addPolyline(mPlops);
            myLine.setWidth(pTrackWidth);
            mPlops.color(track_color[pTrackColor]);
            for(int i=0;i<mLatLngList.size(); i++) {
                LatLng l = mLatLngList.get(i);
                mPlops.add(l);
            }
        }

        if(mCurLoc != null && loc_changed) mPlops.add(new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude()));
        myLine = mMap.addPolyline(mPlops);
        mPlops.color(track_color[pTrackColor]);
        myLine.setWidth(pTrackWidth);

    }

    public long  distance_of_curtrack() {
        long dist = 0;
        LatLng prev = null;
        for(int i=0;i<mLatLngList.size();i++) {
                LatLng l = mLatLngList.get(i);
                if(prev == null) {
                    prev = l;
                    dist = 0;
                } else {
                    CalDistance cd = new CalDistance(prev.latitude, prev.longitude, l.latitude, l.longitude);
                    dist += cd.getDistance();
                    prev = l;
                }
        }
        return dist;
    }

    public long distance_of_track(int start, int end) {
        long dist = 0;
        LatLng prev = null;
        for(int i=start;i<end;i++) {
            LatLng l = mLatLngList.get(i);
            if(prev == null) {
                prev = l;
                dist = 0;
            } else {
                CalDistance cd = new CalDistance(prev.latitude, prev.longitude, l.latitude, l.longitude);
                dist += cd.getDistance();
                prev = l;
            }
        }
        return dist;
    }

    public void drawMarkersbyKm() {
        drawMarkersbyM(1000);
    }

    public void drawMarkersbyM(int interval) {
        // Draw Markes by the mLatLng & mLocTime variables;
        long start = System.currentTimeMillis();
        long mid, end;
        Vector llv = new Vector();
        for(int i=0;i<mLatLngList.size();i++) {
            Hashtable ht = new Hashtable();
            ht.put("latitude",  mLatLngList.get(i).latitude );
            ht.put("longitude", mLatLngList.get(i).longitude);
            ht.put("added_on",  mLocTime.get(i));  // long type
            llv.add(ht);
        }

        mid = System.currentTimeMillis(); Log.e(TAG, "Vector" + (mid-start) + "ms"); start = mid;

        if (llv == null) return;

        int i = 0;
        int dist = 0;
        int lastKm = 1;
        LatLng prev = null;

        for(int k=0;k<llv.size();k++) {
            Hashtable ht = (Hashtable)llv.elementAt(k);
            LatLng l = new LatLng((double)ht.get("latitude"), (double)ht.get("longitude"));

            long added_on = (long)ht.get("added_on");
            SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String added_on_str  = dayTime.format(new Date(added_on));


            if (prev == null) {
                prev = l;
                dist = 0;

                if(pdrawGroudNum) {
                    GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.num128_g_0))
                            .position(l, 64f, 64f);
                    mMap.addGroundOverlay(newarkMap);
                }

                if(pdrawMarker) {
                    long t = added_on;
                    SimpleDateFormat dT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String title = dT.format(new Date(t));

                    MarkerOptions opt = new MarkerOptions()
                            .position(l)
                            .title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .draggable(true).visible(true).snippet("0");
                    mMap.addMarker(opt).showInfoWindow();
                }

            } else {
                CalDistance cd = new CalDistance(prev.latitude, prev.longitude, l.latitude, l.longitude);
                dist += cd.getDistance();

                if (dist > lastKm * interval || k==llv.size()-1) {
                    float  markercolor = BitmapDescriptorFactory.HUE_GREEN;
                    if(k==llv.size()-1) markercolor = BitmapDescriptorFactory.HUE_RED;

                    int step_num = 0;
                    step_num = dist/interval;

                    int img = 0;
                    switch( step_num % 20 ) {
                        case 1: img = R.drawable.num128_g_1; break;
                        case 2: img = R.drawable.num128_g_2; break;
                        case 3: img = R.drawable.num128_g_3; break;
                        case 4: img = R.drawable.num128_g_4; break;
                        case 5: img = R.drawable.num128_g_5; break;
                        case 6: img = R.drawable.num128_g_6; break;
                        case 7: img = R.drawable.num128_g_7; break;
                        case 8: img = R.drawable.num128_g_8; break;
                        case 9: img = R.drawable.num128_g_9; break;
                        case 10:img = R.drawable.num128_0; break;
                        case 11:img = R.drawable.num128_1; break;
                        case 12:img = R.drawable.num128_2; break;
                        case 13:img = R.drawable.num128_3; break;
                        case 14:img = R.drawable.num128_4; break;
                        case 15:img = R.drawable.num128_5; break;
                        case 16:img = R.drawable.num128_6; break;
                        case 17:img = R.drawable.num128_7; break;
                        case 18:img = R.drawable.num128_8; break;
                        case 19:img = R.drawable.num128_9; break;
                        case 0:img = R.drawable.num128_g_0; break;
                        default: img = R.drawable.map_marker_128; break;
                    }

                    try {

                            if(pdrawGroudNum) {
                                GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                                        .image(BitmapDescriptorFactory.fromResource(img))
                                        .position(l, 64f, 64f);
                                mMap.addGroundOverlay(newarkMap);
                            }

                            String dist_str = new String();
                            float distf = dist;
                            float distk = distf / 1000f;
                            if (dist > 100000) {  /* 100 km */
                                dist_str = String.format("%.1fkm", distk);
                            } else if (dist > 10000) {   /* 10 km */
                                dist_str = String.format("%.2fkm", distk);
                            } else if (dist > 1000) {    /* 1km */
                                dist_str = String.format("%.3fkm", distk);
                            } else {
                                dist_str = String.format("%3dmeters", dist);
                            }

                            if(pdrawMarker ) {

                                if( k==llv.size()-1) {
                                } else {
                                    long t = added_on;
                                    SimpleDateFormat dT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                    String title = dT.format(new Date(t));

                                    MarkerOptions opt = new MarkerOptions()
                                            .position(l)
                                            .title(title)
                                            .icon(BitmapDescriptorFactory.defaultMarker(markercolor))
                                            .draggable(true).visible(true).snippet("" + dist_str);
                                    Marker marker = mMap.addMarker(opt);
                                    marker.showInfoWindow();
                                }
                            }

                    }catch(Exception e) {
                        Log.e(TAG,">>>>>>" + e.toString());
                        e.printStackTrace();
                    }
                    lastKm++;
                }
                prev = l;
            }
        }
        mid = System.currentTimeMillis(); Log.e(TAG, "For~" + (mid-start) + "ms"); start = mid;
    }

    public void drawMarkers() {
        for(int i=0; i < mLatLngList.size(); i++) {
            LatLng ll = new LatLng(mLatLngList.get(i).latitude, mLatLngList.get(i).longitude);
            float color = (i==0) ?  BitmapDescriptorFactory.HUE_GREEN : ((i==mLatLngList.size()-1)? BitmapDescriptorFactory.HUE_RED  :  BitmapDescriptorFactory.HUE_CYAN);

            mMap.addMarker( new MarkerOptions()
                    .position(ll)
                    .title("" + (i+1))
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .draggable(true).visible(true).snippet("l/l:" + ll.latitude + "/" + ll.longitude)
            );
        }
    }

    public void drawMarkersDB() {
        Vector v = dbgateway.allLatLngVector(getApplicationContext());

        for(Object obj : v) {
            Hashtable ht = (Hashtable)obj;

            mMap.addMarker( new MarkerOptions()
                    .position(new LatLng((double)ht.get("latitude"), (double)ht.get("longitude")))
                    .title("" + ht.get("added_on"))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .draggable(true).visible(true).snippet("l/l:" + ht.get("latitude") + "/" + ht.get("longitude"))
            );
        }
    }

    private void drawMyMarker(LatLng pos) {
//        LinearLayout tv = (LinearLayout) this.getLayoutInflater().inflate(R.layout.markerdialog, null, false);
//        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
//
//        tv.setDrawingCacheEnabled(true);
//        tv.buildDrawingCache();
//        Bitmap bm = tv.getDrawingCache();
//        LatLng latLng = pos;
//        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bm);
//        BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
//        mMap.addMarker(new MarkerOptions().position(pos).title("origin").snippet("Srivastava").icon(icon));
    }


    private void drawStartingMarker() {
        if(mLatLngList.size() < pStartPos) pStartPos=0; mEditor.putInt("pStartPos", pStartPos); mEditor.commit();

        LatLng ll = mLatLngList.get(pStartPos);

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String str = dayTime.format(pTime_start);

        mMap.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("" + str + "")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pt48))
                    .draggable(true).visible(true).snippet("l/l:" + ll.latitude + "/" + ll.longitude)
            );
    }

    private void drawRunTrackMarker() {
        int num = mLatLngList.size() - pStartPos;
        int interval = 1;
        if(num>10000) interval = 500;
        else if(num > 3000) interval = num/10;
        else if(num > 1000) interval = 100;
        else if(num > 100) interval = 10;
        else if (num > 10) interval =  5;
        else interval = 2;

        for(int i=pStartPos+1;i<mLatLngList.size()-1;i++) {
            LatLng ll = mLatLngList.get(i);
            mMap.addMarker(new MarkerOptions()
                    .position(ll)
                    .title("")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pt48))
                    .draggable(true).visible(true).snippet("l/l:" + ll.latitude + "/" + ll.longitude)
            );
            i+= interval;
        }
    }

    private void drawStopMarker() {
        LatLng ll = mLatLngList.get(mLatLngList.size()-1);
        if(mCurLoc==null) return;
        if(ll==null) return;

        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String str = dayTime.format(System.currentTimeMillis());

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude()))
                .title("" + str + "")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pt48))
                .draggable(true).visible(true).snippet("l/l:" + ll.latitude + "/" + ll.longitude)
        );
    }

    private void drawLastMarker() {
        if(true) {
            if(mCurLoc != null) {
                long t = mCurLoc.getTime();
                SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String title = dayTime.format(new Date(t));

                long d = distance_of_curtrack();
                String dstr;
                if(d > 1000) dstr = "" + (d/1000) + " km";
                else dstr = "" + d + "meters";
                String sippet_str  =  dstr;

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mCurLoc.getLatitude(), mCurLoc.getLongitude()))
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .draggable(true).visible(true).snippet(sippet_str));
            }
        }
    }



    // **  ----------------------------------------------------------------------------------------
    //        Option Menu, 9/29/17, jhpark
    // **  -------------------------------------------------------------------------------------- **


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.runnungmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Option 사용시 다시 그리기를 진행함.
        mPlops = null;

        int id = item.getItemId();
        if (id == R.id.item_dup_remove) {
            duplicateRemove();
            return true;
        }

        if (id == R.id.item_rep_db_with_scr) {
            dbgateway.ListToDB(getApplicationContext(), mLatLngList);
            return true;
        }

        if (id == R.id.item_serialize) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunningActivity.this);
            alertDialog.setTitle("Activity Serialization");
            String  info = "Filename to be serialized:";
            alertDialog.setMessage(info);

            final EditText et = new EditText(RunningActivity.this);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd", Locale.KOREA);
            Date now = new Date();
            final String fileName = "_" + formatter.format(now);


            activity_file_name = mPref.getString("pLatestFilename", fileName);
            if(activity_file_name != null) et.setText(activity_file_name);
            else { et.setText(fileName); activity_file_name = fileName;}


            alertDialog.setView(et);

            alertDialog.setPositiveButton("Serialize", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                if(et.getText()!= null) dbgateway.serailizeActivitywithName(getApplicationContext(), et.getText().toString() + ".ser");
                activity_file_name = et.getText().toString();

                mEditor.putString("pLatestFilename", activity_file_name);
                mEditor.commit();
                getSharedPreferences();

                Toast.makeText(getApplicationContext(),"Activity Saved !!!" + activity_file_name,Toast.LENGTH_LONG).show();
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick (DialogInterface dialog, int whichButton) {
                }
            });

            AlertDialog alert = alertDialog.create();
            alert.show();
            return true;
        }

        if (id == R.id.sync_from_file) {
            mLatLngList = dbgateway.syncLastActivityFilewithScreen(getApplicationContext());
            mLocTime = new Vector(); for(int i=0;i<mLatLngList.size();i++) mLocTime.add(System.currentTimeMillis());

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RunningActivity.this);
            alertDialog.setTitle("Choose an Activity to be reload...");
            String  info = "File to be serialized:";
            alertDialog.setMessage(info);

            final EditText et = new EditText(RunningActivity.this);
            final String fileName = dbgateway.getLastActivityFileName(getApplicationContext());

            if(activity_file_name != null) et.setText(activity_file_name);
            else { et.setText(fileName); activity_file_name = fileName;}

            alertDialog.setView(et);

            alertDialog.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dbgateway.serailizeActivitywithName(getApplicationContext(), fileName);
                    Toast.makeText(getApplicationContext(),"Current Activity Serialized into" +  fileName, Toast.LENGTH_LONG).show();
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick (DialogInterface dialog, int whichButton) {
                }
            });

            AlertDialog alert = alertDialog.create();
            alert.show();
            return true;

        }

        if (id == R.id.item_admin) {
            Intent i = new Intent (RunningActivity.this, AdminActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.item_setting) {
            this.setSharedPrefrences();
            return true;
        }

        if( id == R.id.item_gallery) {
            Uri targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String targetDir = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";   // 특정 경로!!
            targetUri = targetUri.buildUpon().appendQueryParameter("bucketId", String.valueOf(targetDir.toLowerCase().hashCode())).build();
            Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
            startActivity(intent);
            return true;
        }

        if (id == R.id.item_info) {
            File list[] = dbgateway.getallActivities(getApplicationContext());
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Information Brief");
            String  info = "\nLatitude: " + mCurLoc.getLatitude();
                    info+= "\nLongitude: " + mCurLoc.getLongitude();
                    info+= "\nProvider: " + mCurLoc.getProvider();
                    info+= "\n--";

                    info+= "\nNumber of Markers(Screen): " + mLatLngList.size();
                    info+= "\nNumber of Markers(SQLite): " + dbgateway.numberofMarkers(getApplicationContext());

            info+= "\nFile: " + dbgateway.getLastActivityFileName(getApplicationContext());
            alertDialog.setMessage(info);
            alertDialog.setPositiveButton("Okay", null);

            Log.e(TAG,"Info" + info);

            //alertDialog.setButton("OK", null);
            AlertDialog alert = alertDialog.create();
            alert.show();
            return true;
        }

        if (id == R.id.item_list_files) {
            File list[] = dbgateway.getallActivities(getApplicationContext());
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
            alertDialog.setTitle("Choose a file to sync");
            alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                         Log.e(TAG, "" + items[index] + "chosen ");
                         mLatLngList = dbgateway.syncActivityFilewithScreen(getApplicationContext(), filepath[index]);
                         mLocTime = new Vector();for(int i=0;i<mLatLngList.size();i++) mLocTime.add(System.currentTimeMillis());
                         dbgateway.ListToDB(getApplicationContext(), mLatLngList);

                         Log.e(TAG, "" + items[index] + "Sync Ok!");
                         Toast.makeText(getApplicationContext(),"" + items[index] + "Sync Ok!",Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.setNegativeButton("Back",null);

            AlertDialog alert = alertDialog.create();
            alert.show();
            return true;
        }

        if (id == R.id.item_list_pics) {
            int mode = 1;  // 0: scroll view, 1: list item
            final ArrayList<myPicture> list = PhotoUtil.myPictureList;
            if (list == null) return false;
            int msize = list.size();
            final CharSequence items[] = new CharSequence[msize];
            final String filepath[] = new String[msize];

            for(int i=0;i<list.size();i++) {
                items[i] = list.get(i).picname;
                filepath[i] = list.get(i).filepath;

                Log.e(TAG, ""+ i + "picname: " + items[i] + " filenmame:" +filepath[i]);
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            //alertDialog.setIcon(R.drawable.window);
            alertDialog.setTitle("Choose a picture to see");

            if(mode == 0) {
                ScrollView sv = new ScrollView(RunningActivity.this);
                LinearLayout llo = new LinearLayout(RunningActivity.this);
                llo.setOrientation(LinearLayout.VERTICAL);
                sv.addView(llo);

                for(int i=0;i<list.size(); i++) {
                    Bitmap capturebmp = BitmapFactory.decodeFile(filepath[i]);
                    if(capturebmp == null) continue;

                    Log.e(TAG,"file size:" + capturebmp.getByteCount());
                    Log.e(TAG,"width    :" + capturebmp.getWidth());
                    Log.e(TAG,"height   :" + capturebmp.getHeight());

                    ImageView mImageView =  new ImageView(RunningActivity.this);
                    mImageView.setImageBitmap(capturebmp);
                    llo.addView(mImageView);
                }
                alertDialog.setView(sv);
            }

            if(mode == 1) {
                alertDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        Log.e(TAG, "" + items[index] + "chosen ");
                        PhotoUtil.showPictureAlertDialog(RunningActivity.this, (myPicture)list.get(index), index);
                        Log.e(TAG, "" + items[index] + "Pic View Ok!");
                    }
                });
            }

            alertDialog.setNegativeButton("Back",null);
            AlertDialog alert = alertDialog.create();
            alert.show();
            return true;
        }

        // Start External Activity - View CameraPicActivity
        if(id == R.id.item_list_pics_folder) {
            Intent i = new Intent(RunningActivity.this, HistoryActivity.class);
            i.putExtra("historymode", false);
            startActivity(i);
        }

        if(id == R.id.item_show_pics_on_the_map) {
            showPicturesOnMap();
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean showRealPicture = false;
    public void showPicturesOnMap() {
        mMarkerList = new ArrayList<Marker>();
        ArrayList<myPicture> list = PhotoUtil.myPictureList;
        int msize = list.size();
        for (int i = 0; i < list.size(); i++) {

            myPicture mp = list.get(i);
            myActivity ma = mp.myactivity;
            LatLng loc = new LatLng(ma.latitude, ma.longitude);

            MarkerOptions opt = new MarkerOptions()
                    .position(loc)
                    .title(mp.picname)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .draggable(true).visible(true).snippet(ma.added_on);

            if(!showRealPicture)
                opt.icon(BitmapDescriptorFactory.fromResource(R.drawable.custommarker2_48));
            else {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
                    Bitmap bmp2 = PhotoUtil.scaleDown(bmp, 300, false); //small size bitmap
                    opt.icon(BitmapDescriptorFactory.fromBitmap(bmp2));
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                }
            }

            Marker marker = mMap.addMarker(opt);
            marker.setTag(mp); //setTag, getTag with myPicture
            marker.showInfoWindow();
            mMarkerList.add(marker);

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return pictureMarkerClick(marker);
                }
            });
        }
    }

    public boolean pictureMarkerClick(final Marker marker) {

        Log.e(TAG,">>>> onMarkerClick(final Marker marker)!");
        myPicture mp = (myPicture)marker.getTag();
        if(mp==null) return false;

        boolean found = false;
        boolean picturemarker = false;
        int index=0;
        // picture marker 검색
        for(int i=0;i<mMarkerList.size();i++) {
            if(mMarkerList.get(i).equals(marker))  {
                found = true;
                picturemarker = true;
                index = i;
                break;
            }
        }

        // picture marker 인 경우
        if(picturemarker && found) {

//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse("file://" + mp.filepath), "image/*");
//            startActivity(intent);


            PhotoUtil.showPictureAlertDialog(RunningActivity.this, mp, index);
            Log.e(TAG,"Picture Marker Found at : " + index + " th location");


        }
        return true;
    }




    public static boolean tg_show_pics = true;

    public void duplicateRemove() {
        //dbgateway.duplicateRemove();
        this.duplicatedLocRemove();
        dbgateway.ListToDB(getApplicationContext(), mLatLngList);
    }

    public void duplicatedLocRemove() {
        List<LatLng> newList = new ArrayList<LatLng>();
        Vector vt = new Vector();

        int dupcnt = 0;
        for(int i=1;i<mLatLngList.size(); i++) {
            LatLng ll1 = mLatLngList.get(i-1);
            LatLng ll2 = mLatLngList.get(i);
            if(ll1.latitude == ll2.latitude && ll1.longitude == ll2.longitude)  {
                Log.e(TAG, "**** Dup : [" + i + "] = [" + (i-1) + "]" );
            } else {
                newList.add(ll1);
                vt.add(mLocTime.get(i-1));
            }
        }
        if(mLatLngList.size()>1) {
            LatLng ll1 = mLatLngList.get(mLatLngList.size() - 1);
            LatLng ll2 = mLatLngList.get(mLatLngList.size() - 2);
            if(ll1.latitude == ll2.latitude && ll1.longitude == ll2.longitude)  {
                Log.e(TAG, "**** Dup : [" + (mLatLngList.size() - 1) + "] = [" + (mLatLngList.size() - 2) + "]" );
            } else {
                newList.add(ll1);
                vt.add(mLocTime.get(mLatLngList.size() - 1));
            }
        }

        Toast.makeText(getApplicationContext(), "" + (mLatLngList.size() - newList.size()) + " duplicated markers removed!", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "**** Dup removed from " + mLatLngList.size() + " to " + newList.size() );
        mLatLngList = newList;
    }
}
