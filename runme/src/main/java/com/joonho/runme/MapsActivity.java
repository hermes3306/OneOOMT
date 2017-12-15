package com.joonho.runme;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joonho.runme.util.ActivityUtil;
import com.joonho.runme.util.MyActivity;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener  {

    private GoogleMap mMap;
    private String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        Intent intent = getIntent();
        final ArrayList<MyActivity> mList = (ArrayList<MyActivity>)intent.getSerializableExtra("locations");

        int w=0,h=0;

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        drawGoogleMap(mList, mMap, width,height,false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }


    public void drawGoogleMap(ArrayList<MyActivity> mList, GoogleMap gmap, int width, int height, boolean mode_append) {

        ActivityUtil.drawTrack(gmap, mList);
        Log.e(TAG, "ActivityUtil.drawTrack();");

        ActivityUtil.drawMarkers(gmap, mList);
        Log.e(TAG, "ActivityUtil.drawMarkers();");

        ActivityUtil.doBoundBuild(gmap, width, height);
        Log.e(TAG, "ActivityUtil.doBoundBuild();");

        LatLng ll = new LatLng( mList.get(mList.size()-1).latitude, mList.get(mList.size()-1).longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 14));
    }




}
