package com.joonho.myway;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.myway.test.PermissionUtils;
import com.joonho.myway.util.CalBearing;
import com.joonho.myway.util.CalDistance;
import com.joonho.myway.util.Config;
import com.joonho.myway.util.MyActivityUtil;
import com.joonho.myway.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {


    public static String    TAG                     = "MainActivity";
    private boolean         __svc_started           = false;
    private Intent __svc_Intent            = null;
    MyLocationService       mMyLocationService;


    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private Marker mMarker                 = null;
    private Polyline mPolyline               = null;
    TextView tv_status               = null;
    Button bt_track                = null;
    Button                  bt_current              = null;
    Button                  bt_walking              = null;
    Button                  bt_memo                 = null;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if ((ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setOnMapClickListener(this);

        tv_status   = findViewById(R.id.tv_status);
        bt_current  = findViewById(R.id.bt_current);
        bt_memo     = findViewById(R.id.bt_memo);
        bt_track    = findViewById(R.id.bt_track);
        bt_walking  = findViewById(R.id.bt_walking);
    }

    @Override
    public void onMapClick(LatLng point) {
        setStatus(point.toString());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
        CalDistance cd = new CalDistance(curloc.latitude, curloc.longitude, point.latitude, point.longitude);
        String addr = MyActivityUtil.getAddress(getApplicationContext(), point);
        String head = String.format("%.5f",point.latitude) + "," + String.format("%.5f",point.longitude);
               head += "  " + String.format("%.0f",cd.getDistance()) + "m";
        drawMarker(point,head, addr );
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    public void addLoc(Location loc) {
        if(loc==null) {
            setStatus("No GPS");
            return;
        }

        curloc = new LatLng(loc.getLatitude(), loc.getLongitude());
        setStatus(loc.toString());

        MyActivity ma = new MyActivity(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), LocTimeStr(loc));
        drawMarker(ma);
        if(_showtrack) {
            drawTrack(mMyLocationService.getMyAcitivityList(), Color.CYAN,15);
            if(mPolyline!=null) mPolyline.setVisible(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Location loc = mMap.getMyLocation();
        addLoc(loc);
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        addLoc(location);
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(__svc_started) {
            Toast.makeText(MapsActivity.this, "SERVICE ALREADY STARTED", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(MapsActivity.this,"SERVICE STARTED", Toast.LENGTH_SHORT).show();

        Intent myI = new Intent(this, MyLocationService.class);
        bindService(myI, conn, Context.BIND_AUTO_CREATE);
        __svc_started = true;
    }

    @Override
    protected void onStop() {
        //Toast.makeText(MainActivity.this,"onStop()", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doMyTimeTask() {
        TimerTask mTask =new MapsActivity.MyTimerTask();
        Timer mTimer = new Timer();
        mTimer.schedule(mTask, Config._timer_delay, Config._timer_period);
    }

    public static LatLng curloc=null, preloc=null;

    public class MyTimerTask extends java.util.TimerTask{
        public void run() {
            if(__svc_started != true) return;

            long start = System.currentTimeMillis();
            MapsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    MyActivity ma = mMyLocationService.getLastLocation();
                    if(ma==null) {
                        setStatus("No GPS");
                        return;
                    }
                    LatLng curloc = new LatLng(ma.latitude,ma.longitude);
                    if(curloc.equals(preloc)) return;

                    drawMarker(ma);
                    Config._myzoom = mMap.getCameraPosition().zoom;

                    if(Config._driving_mode) {
                        double mbearing = 0;
                        if(preloc !=null && preloc != curloc) {
                            CalBearing cb = new CalBearing(preloc.latitude, preloc.longitude, curloc.latitude, curloc.longitude);
                            mbearing = cb.getBearing();
                        }
                        CameraPosition currentPlace = new CameraPosition.Builder()
                                .target(curloc)
                                .bearing((float)mbearing).zoom(Config._myzoom).build();
                        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
                    } else {
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(Config._myzoom).build();
                        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                    preloc = curloc;
                    setStatus("new("+ma.latitude + "," +  ma.longitude+")");
                }
            });
        } /* end of run() */
    } /* end of MyTimerTask */

    public void setStatus(String str) {
        tv_status.setText(StringUtil.DateToString(new Date(), "hh:mm:ss") + "-" + str);
    }

    public String LocTimeStr(Location loc) {
        String added_on = StringUtil.DateToString(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
        return added_on;
    }

    public static boolean _showtrack=true;
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_current:
                Location loc = mMyLocationService.getLocation();
                if(loc!=null) curloc = new LatLng(loc.getLatitude(), loc.getLongitude());
                else {
                    setStatus("No GPS");
                    return;
                }
                MyActivity ma = new MyActivity(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), LocTimeStr(loc));
                drawMarker(ma);

                if(_showtrack) {
                    drawTrack(mMyLocationService.getMyAcitivityList(), Color.CYAN,15);
                    mPolyline.setVisible(true);
                }
                break;

            case R.id.bt_track:
                if(_showtrack) {
                    drawTrack(mMyLocationService.getMyAcitivityList(),Color.CYAN,15);
                    if(mPolyline!=null) mPolyline.setVisible(true);
                    else setStatus("No Track");
                    _showtrack=false;
                }else {
                    if(mPolyline!=null) mPolyline.setVisible(false);
                    else setStatus("No Track");
                    _showtrack=true;
                }
                break;

            case R.id.bt_memo:
                break;

            case R.id.bt_walking:
                Location walkloc = mMyLocationService.getLocation();
                if(walkloc==null) {
                    setStatus("No GPS");
                    return;
                }
                LatLng   ll  = new LatLng(walkloc.getLatitude(),walkloc.getLongitude());
                double mbearing = 0;
                MyActivity ma2 = mMyLocationService.getLastLocation();
                MyActivity ma1 = mMyLocationService.getLastLastLocation();
                if(ma1 !=null && ma2 != null) {
                    CalBearing cb = new CalBearing(ma1.latitude, ma1.longitude, ma2.latitude, ma2.longitude);
                    mbearing = cb.getBearing();
                }
                CameraPosition currentPlace = new CameraPosition.Builder()
                        .target(ll)
                        .bearing((float)mbearing).zoom(Config._myzoom).build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
                if(_showtrack) {
                    drawTrack(mMyLocationService.getMyAcitivityList(),Color.CYAN,15);
                    mPolyline.setVisible(true);
                }
                break;

            case R.id.bt_list:
                File files_nav_list[] = MyActivityUtil.getFiles();
                if(files_nav_list == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_SHORT).show();
                    return;
                }
                int files_nav_list_size = files_nav_list.length;
                final CharSequence items2[] = new CharSequence[files_nav_list_size];
                final String filepath2[] = new String[files_nav_list_size];

                for(int i=0;i<files_nav_list_size;i++) {
                    long sz = files_nav_list
                            [i].length();
                    String _sz=null;
                    if(sz > 1024*1024) _sz = "" + sz / (1024 * 1024) + "MB";
                    else if(sz > 1024) _sz = "" + sz / (1024) + "KB";
                    else _sz = "" + sz + "B";
                    items2[i] = files_nav_list[i].getName() + "(" + _sz + ")" ;
                    filepath2[i] = files_nav_list[i].getAbsolutePath();
                }

                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                alertDialog2.setTitle("Select an activity on the mobile("+files_nav_list_size+")");
                alertDialog2.setItems(items2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        File afile = new File(filepath2[index]);
                        Intent intent = new Intent(MapsActivity.this, FileActivity.class);
                        intent.putExtra("file", afile.getAbsolutePath());
                        intent.putExtra("pos", index);
                        startActivity(intent);
                    }
                });
                alertDialog2.setNegativeButton("Back",null);
                AlertDialog alert2 = alertDialog2.create();
                alert2.show();
                break;

            case R.id.bt_dododo:
                MyActivityUtil.dododo();
                break;
            case R.id.bt_camera:
                ArrayList<MyActivity> mlist = mMyLocationService.getMyAcitivityList();
                String fname = Config.get_filename();
                MyActivityUtil.serializeActivityIntoFile(mlist, fname);
                Intent intent = new Intent(MapsActivity.this, FileActivity.class);
                intent.putExtra("file", Config.getAbsolutePath(fname));
                intent.putExtra("pos", 0);
                startActivity(intent);
                break;
        }
    }

    /* Map functions */
    public void drawMarker(LatLng l, String head, String body) {
        if(mMarker==null) {
            MarkerOptions opt = new MarkerOptions()
                    .position(l)
                    .title(head)
                    .icon(BitmapDescriptorFactory.defaultMarker(Config._marker_color))
                    .draggable(true).visible(true).snippet(body);
            mMarker = mMap.addMarker(opt);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(l).zoom(Config._myzoom).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            mMarker.setPosition(l);
            mMarker.setTitle(head);
            mMarker.setSnippet(body);
        }
    }

    public void drawMarker(MyActivity ma) {
        String _head = StringUtil.DateToString(new Date(), "hh:mm:ss");
        String _body = MyActivityUtil.getAddress(getApplicationContext(),ma);
        LatLng l = new LatLng(ma.latitude,ma.longitude);
        drawMarker(l,_head,_body);
    }

    public void drawTrack(ArrayList<MyActivity> list, int color, int width ) {
        if(list==null) return;
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=0; i<list.size();i++) {
            l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        if(mPolyline==null) {
            PolylineOptions plo = new PolylineOptions();
            plo.color(color);
            mPolyline = mMap.addPolyline(plo);
            mPolyline.setPoints(l);
            mPolyline.setWidth(width);
        }else {
            mPolyline.setColor(color);
            mPolyline.setPoints(l);
            mPolyline.setWidth(width);
        }
    }
}