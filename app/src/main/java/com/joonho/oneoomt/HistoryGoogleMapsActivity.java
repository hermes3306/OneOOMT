package com.joonho.oneoomt;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.db.PropsDB;
import com.joonho.oneoomt.util.CalDistance;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class HistoryGoogleMapsActivity extends AppCompatActivity /*FragmentActivity*/ implements OnMapReadyCallback, LocationListener {
    private static final String TAG = "HistoryGoogleMaps";

    private GoogleMap mMap;
    private double latitudes[];
    private double longitudes[];
    private ArrayList<String> added_ons;
    private String fname;
    private static DBGateway dbgateway=new DBGateway();

    private static final int myzoom = 12;
    List<LatLng> llv = new ArrayList<>();
    private static PropsDB pdb = new PropsDB();

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    LocationManager locationManager;
    LatLng myCoordinates;
    MarkerOptions mo;
    Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_google_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else requestLocation();
        if (!isLocationEnabled()) showAlert(1);
    }

    public void drawTrack(List<LatLng> llv) {
        if (llv == null) return;

        PolylineOptions plo = new PolylineOptions();
        plo.width(15);
        plo.color(Color.BLUE);
        Polyline line = mMap.addPolyline(plo);
        line.setPoints(llv);
    }

    public Vector toVector() {
        Vector vt = new Vector();

        Intent intent = getIntent();
        double lats[]  = intent.getDoubleArrayExtra("latitudes");
        double longs[]  = intent.getDoubleArrayExtra("longitudes");
        ArrayList<String> adds = intent.getStringArrayListExtra("added_ons");
        fname = intent.getStringExtra("fname");

        for (int i = 0; i < latitudes.length; i++) {
            Hashtable ht = new Hashtable();
            ht.put("latitude", lats[i]);
            ht.put("longitude", longs[i]);
            ht.put("added_on", adds.get(i));

            vt.add(ht);
        }
        if (vt == null) return null;
        return vt;
    }

    public void drawMarkersbyKm() {
        Vector vt = toVector();
        if (vt == null) return;

        int i = 0;
        int dist = 0;
        int lastKm = 1;
        LatLng prev = null;

        for(int k=0;k<llv.size();k++) {
            Hashtable ht = (Hashtable)vt.elementAt(k);
            LatLng l = new LatLng((double)ht.get("latitude"), (double)ht.get("longitude"));
            String added_on = (String)ht.get("added_on");

            if (prev == null) {
                prev = l;
                dist = 0;
                MarkerOptions opt = new MarkerOptions()
                        .position(l)
                        .title("" + added_on)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(true).visible(true).snippet("" + 0 + "km");
                mMap.addMarker(opt).showInfoWindow();
            } else {
                CalDistance cd = new CalDistance(prev.latitude, prev.longitude, l.latitude, l.longitude);
                dist += cd.getDistance();

                if (dist > lastKm * 1000 || k==llv.size()-1) {
                    float  markercolor = BitmapDescriptorFactory.HUE_GREEN;
                    if(k==llv.size()-1) markercolor = BitmapDescriptorFactory.HUE_MAGENTA;

                    float lastKmf = 0f;

                    lastKmf = dist/1000f;

                    String lastKmstr = "" + lastKmf;
                    lastKmstr = lastKmstr.substring(0,3);

                    MarkerOptions opt = new MarkerOptions()
                            .position(l)
                            .title("" + added_on)
                            .icon(BitmapDescriptorFactory.defaultMarker(markercolor))
                            .draggable(true).visible(true).snippet("" + lastKmstr + "km");


                    mMap.addMarker(opt).showInfoWindow();

                    Log.e(TAG, "*******" + dist / 1000 + " Km");
                    lastKm++;
                }
                prev = l;
            }
        }
    }


    public void drawMarkers() {
        if (llv == null) return;
        if (pdb.getProperty(getApplicationContext(), "DRAW_MARKERS_ON_HISTORY") == null) return;
        if (pdb.getProperty(getApplicationContext(), "DRAW_MARKERS_ON_HISTORY").equalsIgnoreCase("false")) {
            return;
        }
        String marker_interval_str = pdb.getProperty(getApplicationContext(), "MARKER INTERVAL");
        int marker_interval = 1;
        if (marker_interval_str != null) {
            marker_interval = Integer.parseInt(marker_interval_str);
        }

        int i = 0;
        for (LatLng l : llv) {
            if (i == 0) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title("" + added_ons.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .draggable(true).visible(true).snippet("l/l:" + l.latitude + "/" + l.longitude));
            }
            i++;
            if (i % marker_interval == 0) i = 0;
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

        historyview();
        drawMarkersbyKm();
        btn_event();
    }

    public void btn_event() {
        final Button bt_redraw = (Button)findViewById(R.id.bt_redraw);
        final Button bt_delete = (Button)findViewById(R.id.bt_delete);
        final Button bt_rename = (Button)findViewById(R.id.bt_rename);


        bt_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File f = new File(fname);
                f.delete();
                Toast.makeText(HistoryGoogleMapsActivity.this, "File(" + fname + ") deleted OK!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        bt_rename.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(HistoryGoogleMapsActivity.this);
                alertDialog.setTitle("Rename Activity");

                final File f = new File(fname);
                final EditText et = new EditText(HistoryGoogleMapsActivity.this);

                et.setText(f.getName());
                alertDialog.setView(et);

                alertDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File dest = new File(f.getParentFile(), et.getText().toString());
                        f.renameTo(   dest    );
                        fname = f.getAbsolutePath();
                        Toast.makeText(HistoryGoogleMapsActivity.this, "Rename(" + fname + ") OK!", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });


        /* Progress Bar Required !!! */
        bt_redraw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dbgateway.deserializeToDB(getApplication(), fname);
                Toast.makeText(getApplicationContext(),"" + fname + " Reloaded !! \n Refresh on Map", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void historyview() {
        Intent intent = getIntent();
        latitudes = intent.getDoubleArrayExtra("latitudes");
        longitudes = intent.getDoubleArrayExtra("longitudes");
        added_ons = intent.getStringArrayListExtra("added_ons");
        fname = intent.getStringExtra("fname");

        for (int i = 0; i < latitudes.length; i++) {
            Log.e(TAG, "l/l/a = (" + latitudes[i] + "," + longitudes[i] + "," + added_ons.get(i) + ")");
            LatLng ll = new LatLng(latitudes[i], longitudes[i]);
            llv.add(ll);
        }

        if (llv == null) return;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitudes[latitudes.length - 1], longitudes[longitudes.length - 1]);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Last Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, myzoom));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, myzoom));

        drawTrack(llv);
        marker = mMap.addMarker(mo);

    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());



        mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(myCoordinates);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);


        drawMarkersbyKm();

        MarkerOptions opt = new MarkerOptions()
                .position(myCoordinates)
                .title("" + new Date())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                .draggable(true).visible(true).snippet("New Location" + myCoordinates);

        Toast.makeText(getApplicationContext(),"Location Changed~", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 1000, 5, this);
    }

    private boolean isPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission is granted");
            return true;
        } else {
            Log.e(TAG, "Permission not granted");
            return false;
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if(status ==1) {
            message = "Your Locatoin Settings is set to 'Off'/\nPlease Enable Location to " +
                    "use the app";
            title = "Enable Location";
            btnText = "Location Settings";
        }else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        if(status ==1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        finish();
                    }
                });
         dialog.show();
    }
}


