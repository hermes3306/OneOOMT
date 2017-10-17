package com.joonho.oneoomt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.db.PropsDB;
import com.joonho.oneoomt.util.CalDistance;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static android.view.View.VISIBLE;

public class MapsActivity extends AppCompatActivity /*FragmentActivity*/ implements OnMapReadyCallback {
    private final String TAG = "MapsActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private GoogleMap mMap;
    int myzoom = 12;
    private int mtype = GoogleMap.MAP_TYPE_NORMAL;
    private PropsDB probs = new PropsDB();

    private static DBGateway dbgateway=new DBGateway();
      private static final int storageopt = 0; //0: DB, 1: Memory


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent myI = new Intent(this, MyLocationService.class);
        startService(myI);
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
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//
        LatLng lastloc = dbgateway.lastLatLng(MapsActivity.this);
        int sizeofdb = dbgateway.LocSize(MapsActivity.this);

        mMap.clear();
        if(lastloc != null) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastloc, myzoom));
            drawTrack();
            drawMarkersbyKm();

        }


        LinearLayout ll_upper_buttons = (LinearLayout)findViewById(R.id.ll_upper_buttons);
        String show_up_menu = probs.getProperty(getApplicationContext(), "Up_Button_Menu");
        Log.e(TAG, "****Up_Button_Menu: " + show_up_menu);
        if (show_up_menu==null || show_up_menu.equalsIgnoreCase("false")) {
            // do nothing
            ll_upper_buttons.setVisibility(View.GONE);
        } else {
            ll_upper_buttons.setVisibility(VISIBLE);
        }

        btn_bottom_event();
        btn_top_event();
        btn_menu_transparent_event();

        Toast.makeText(getApplicationContext(),"Total " + sizeofdb + " Markers Recorded!",Toast.LENGTH_LONG).show();
    }



    public void btn_top_event() {
        final Button bt_save, bt_hist,bt_conf, bt_debug, bt_camera;
        bt_save = (Button)findViewById(R.id.bt_save);
        bt_hist = (Button)findViewById(R.id.bt_hist);
        bt_conf = (Button)findViewById(R.id.bt_conf);
        bt_camera = (Button)findViewById(R.id.bt_camera);

        bt_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               btn_save();
            }}
        );


        bt_hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dbgateway.getallActivities(MapsActivity.this);
                Intent i = new Intent(MapsActivity.this, HistoryActivity.class);
                startActivity(i);
            }}
        );

        bt_conf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, PropsActivity.class);
                startActivity(i);
            }
        });

        bt_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Tacke a picture 2 ~", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
            }}
        );
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
                //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File storageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");

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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //File storageDir = Environment.getExternalStorageDirectory();
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


//    private int imagepos=1;
//    final int img_view_id[] = new int [] {
//            R.id.imageView1,
//            R.id.imageView2,
//            R.id.imageView3,
//            R.id.imageView4,
//            R.id.imageView5,
//            R.id.imageView6,
//            R.id.imageView7,
//            R.id.imageView8,
//            R.id.imageView9,
//            R.id.imageView10
//    };


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//
//            ImageView mImageView = findViewById(img_view_id[imagepos++]);
//            if(imagepos==9) imagepos=0;
//            mImageView.setImageBitmap(imageBitmap);
//        }
//    }

    public void btn_menu_transparent_event() {
        final ImageButton imb_menu_trans = (ImageButton) findViewById(R.id.imgbt_menu_popup);
        imb_menu_trans.setBackgroundColor(Color.TRANSPARENT);
        imb_menu_trans.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(MapsActivity.this, v);
                getMenuInflater().inflate(R.menu.mainoptmenu2, p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
                p.show();
            }
        });

        final ImageButton imb_zoomin = (ImageButton) findViewById(R.id.imgbt_zoomin);
        final ImageButton imb_zoomout = (ImageButton) findViewById(R.id.imgbt_zoomout);
        final ImageButton imb_refresh = (ImageButton) findViewById(R.id.imgbt_refresh);
        final ImageButton imb_hist = (ImageButton) findViewById(R.id.imb_hist);
        final ImageButton imb_save = (ImageButton) findViewById(R.id.imb_save);
        final ImageButton imb_pic = (ImageButton) findViewById(R.id.imb_pic);

        imb_zoomin.setBackgroundColor(Color.TRANSPARENT);
        imb_zoomout.setBackgroundColor(Color.TRANSPARENT);
        imb_refresh.setBackgroundColor(Color.TRANSPARENT);
        imb_hist.setBackgroundColor(Color.TRANSPARENT);
        imb_save.setBackgroundColor(Color.TRANSPARENT);
        imb_pic.setBackgroundColor(Color.TRANSPARENT);


        imb_zoomin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        imb_zoomout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        imb_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear();
                drawTrack();
                drawMarkersbyM(1000); //for each 10 meters
                LatLng lastloc = dbgateway.lastLatLng(MapsActivity.this);
                int sizeofdb = dbgateway.LocSize(MapsActivity.this);
                if(lastloc != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastloc, myzoom));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(lastloc));
                }
            }
        });

        imb_hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        imb_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_save();
            }
        });

        imb_pic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
       
    }

    public void btn_bottom_event () {

        final Button bt_popup_menu = (Button)findViewById(R.id.bt_popup_menu);
        bt_popup_menu.setVisibility(VISIBLE);
        bt_popup_menu.setBackgroundColor(Color.TRANSPARENT);

        bt_popup_menu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(MapsActivity.this, v);
                getMenuInflater().inflate(R.menu.mainoptmenu2, p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
                p.show();
            }
        });

        final Button button1 = (Button) findViewById(R.id.mybutton1);
        button1.setBackgroundColor(Color.TRANSPARENT);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        final Button button2 = (Button) findViewById(R.id.mybutton2);
        button2.setBackgroundColor(Color.TRANSPARENT);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        final Button button3 = (Button) findViewById(R.id.btn_Mark);
        button3.setBackgroundColor(Color.TRANSPARENT);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
            }
        });

        final Button btn_type = (Button) findViewById(R.id.btn_type);
        btn_type.setBackgroundColor(Color.TRANSPARENT);
        btn_type.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mtype == GoogleMap.MAP_TYPE_NORMAL) mtype = GoogleMap.MAP_TYPE_SATELLITE;
                else mtype = GoogleMap.MAP_TYPE_NORMAL;
                mMap.setMapType(mtype);
            }
        });

    }

    public void drawTrack() {
        PolylineOptions plo = new PolylineOptions();
        plo.width(15);
        plo.color(Color.BLUE);
        Polyline line = mMap.addPolyline(plo);
        List<LatLng> llv = dbgateway.allLatLng(getApplicationContext());
        if (llv==null) return;
        line.setPoints(llv);
    }


    public void drawMarkersbyKm() {
        Vector llv = dbgateway.allLatLngVector(getApplicationContext());
        if (llv == null) return;

        int i = 0;
        int dist = 0;
        int lastKm = 1;
        LatLng prev = null;

        for(int k=0;k<llv.size();k++) {
            Hashtable ht = (Hashtable)llv.elementAt(k);
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


    public void drawMarkersbyM(int interval) {
        Vector llv = dbgateway.allLatLngVector(getApplicationContext());
        if (llv == null) return;

        int i = 0;
        int dist = 0;
        int lastKm = 1;
        LatLng prev = null;

        for(int k=0;k<llv.size();k++) {
            Hashtable ht = (Hashtable)llv.elementAt(k);
            LatLng l = new LatLng((double)ht.get("latitude"), (double)ht.get("longitude"));
            String added_on = (String)ht.get("added_on");

            if (prev == null) {
                prev = l;
                dist = 0;
                MarkerOptions opt = new MarkerOptions()
                        .position(l)
                        .title("" + added_on)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(true).visible(true).snippet("" + 0 + "meters");
                mMap.addMarker(opt).showInfoWindow();
            } else {
                CalDistance cd = new CalDistance(prev.latitude, prev.longitude, l.latitude, l.longitude);
                dist += cd.getDistance();

                if (dist > lastKm * interval || k==llv.size()-1) {
                    float  markercolor = BitmapDescriptorFactory.HUE_GREEN;
                    if(k==llv.size()-1) markercolor = BitmapDescriptorFactory.HUE_MAGENTA;

                    float lastKmf = 0f;

                    float intervalf = interval;
                    lastKmf = dist/intervalf;

                    String lastKmstr = "" + lastKmf * interval;
                    lastKmstr = lastKmstr.substring(0,5);

                    MarkerOptions opt = new MarkerOptions()
                            .position(l)
                            .title("" + added_on)
                            .icon(BitmapDescriptorFactory.defaultMarker(markercolor))
                            .draggable(true).visible(true).snippet("" + lastKmstr + "meters");


                    mMap.addMarker(opt).showInfoWindow();

                    Log.e(TAG, "*******" + dist  + " meters");
                    lastKm++;
                }
                prev = l;
            }
        }
    }

    public void drawMarkers2() {
        List<LatLng> llv = dbgateway.allLatLng(getApplicationContext());
        Toast.makeText(MapsActivity.this, "" + llv.size(), Toast.LENGTH_SHORT).show();
        for(LatLng ll : llv) {
            mMap.addMarker( new MarkerOptions()
                    .position(ll)
                    .title("Current Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .draggable(true).visible(true).snippet("l/l:" + ll.latitude + "/" + ll.longitude)
            );

        }
    }

    public void drawMarkers() {
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

    private void drawMarker() {
        Hashtable loc = dbgateway.lastLatLngHashtable(getApplicationContext());
        if (loc == null) return;

        mMap.addMarker(new MarkerOptions()
                .position(dbgateway.lastLatLng(getApplicationContext()))
                .title("" + loc.get("added_on"))
                .draggable(true).visible(true).snippet("l/l:" + loc.get("latitude") + "/" + loc.get("longitude"))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainoptmenu, menu);
        return true;
    }

    private boolean marker_enabled=true;
    private boolean track_enabled=true;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.main_opt_menu_item_save) {
            btn_save();
            return true;
        }

        if (id == R.id.main_opt_menu_item_hist) {
            dbgateway.getallActivities(MapsActivity.this);
            Intent i = new Intent(MapsActivity.this, HistoryActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.main_opt_menu_item_conf) {
            Intent i = new Intent(MapsActivity.this, PropsActivity.class);
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
        String ret = dbgateway.serializeActivities(MapsActivity.this);
        if(ret==null) {
            Toast.makeText(getApplicationContext(),"Tracking Information Saving Failed!!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Activity Saved ("+ret+")!!!", Toast.LENGTH_SHORT).show();
        }

        String delete_when_save_opt = probs.getProperty(getApplicationContext(), "DELETE_WHEN_SAVE_ACT");
        Log.e(TAG, "****DELETE_WHEN_SAVE_ACT: " + delete_when_save_opt);

        if (delete_when_save_opt==null) return;

        if (delete_when_save_opt.equalsIgnoreCase("true")) {
            dbgateway.dbresetwithoutloastloc(MapsActivity.this);
        }
    }


}


