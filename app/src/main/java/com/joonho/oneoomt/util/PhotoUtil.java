package com.joonho.oneoomt.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.joonho.oneoomt.R;
import com.joonho.oneoomt.RunningActivity;
import com.joonho.oneoomt.ViewCameraPicActivity;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.file.myPicture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by joonhopark on 2017. 9. 17..
 */

public class PhotoUtil {
    public static File mediaStorageDir =  null;
    public static File backupDir = null;

    public static String TAG = "PhotoUtil";
    public static String myPictureMetaFilename = "myPicture20.master";
    public static ArrayList<myPicture> myPictureList = new ArrayList<myPicture> ();
    public static int position=0;
    public static boolean ADMIN_MODE=true;

    static {
//        mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OneOOMT");
        String backupdir = StringUtil.DateToString1(new Date(), "yyyyMMdd");
        backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OneOOMT" + backupdir);
        myPictureList = loadMyPictueList();

        // 초기화
        //myPictureList = new ArrayList<myPicture>();
    }

    public static int picsize = 100;
    public static void validatePictures(final Context context) {
        if(myPictureList.size()==0) return;
        final Boolean dl[] = new Boolean[myPictureList.size()];

        new AsyncTask<Long,Void,Long>() {

            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Long doInBackground(Long... longs) {
                int msize = myPictureList.size();
                asyncDialog.setMax(msize);
                for(int i=0;i<msize;i++) {


                    myPicture mp = myPictureList.get(i);
                    asyncDialog.setProgress(i);

                    Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
                    if(bmp == null){
                        dl[i] = true;
                    }else {
                        dl[i] = false;
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
            }

            @Override
            protected void onPostExecute(Long aLong) {
                super.onPostExecute(aLong);
                for(int i=myPictureList.size()-1; i>=0; i--) {
                    if(dl[i]) myPictureList.remove(i);
                }
                asyncDialog.dismiss();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onCancelled(Long aLong) {
                super.onCancelled(aLong);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }
        }.execute();
    }


    public static ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER;
    public static void showPictureAlertDialog(Context ctx, myPicture mp, int index) {
        final int inx = index;
        final Context cur_context = ctx;
        position=index;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        //alertDialog.setIcon(R.drawable.window);
        if(mp.filepath == null) {
            Toast.makeText(ctx, "File not found!", Toast.LENGTH_LONG).show();
            return;
        }

//        Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
//        if(bmp == null){
//            Toast.makeText(ctx, "Error while decoding image file " + mp.filepath, Toast.LENGTH_LONG).show();
//            return;
//        }

        final AlertDialog.Builder _alertDialog = alertDialog;
        //alertDialog.setTitle(mp.picname);

        final LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.VERTICAL);

        FrameLayout flo = new FrameLayout(ctx);
        FrameLayout.LayoutParams floparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        flo.setLayoutParams(floparams);

        final ImageView mImageView =  new ImageView(ctx);
        //Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
        Bitmap bmp3 = PhotoUtil.getPreview(mp.filepath, 1400);

        mImageView.setImageBitmap(bmp3);
        mImageView.setScaleType(scaleType);

//        if(bmp.getWidth() > bmp.getHeight()) mImageView.setRotation(90);
        final LinearLayout ll09 = new LinearLayout(ctx);
        ll09.setOrientation(LinearLayout.HORIZONTAL);
        ImageButton imbt_prev = new ImageButton(ctx);
        imbt_prev.setImageResource(R.drawable.arrow_left128);
        imbt_prev.setBackgroundColor(Color.TRANSPARENT);
        imbt_prev.setAlpha(0.62f);
        ImageButton imbt_next = new ImageButton(ctx);
        imbt_next.setImageResource(R.drawable.arrow_right128);
        imbt_next.setBackgroundColor(Color.TRANSPARENT);
        imbt_next.setAlpha(0.62f);


        ll09.addView(imbt_prev);
        ll09.addView(imbt_next);

        ll09.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        ll09.setVerticalGravity(Gravity.CENTER_VERTICAL);

        LatLng startpos = RunningActivity.getCurLatLng();
        String sinfo = null;
        if(startpos != null) {
            CalDistance cd = new CalDistance(mp.myactivity.latitude, mp.myactivity.longitude, startpos.latitude, startpos.longitude);
            double minDist = cd.getDistance();
            final String _minDist = (minDist>1000)?  "" + (int)(minDist/1000) + "킬로" : "" + (int)minDist + "미터";
            Date date = StringUtil.StringToDate1(mp.picname);
            String date_str = StringUtil.DateToString1(date, "MM월 dd일 HH시");
            sinfo = "" + date_str + "(" + _minDist + ")";
        }

        String inx_str = "" + (index+1) + "/" + myPictureList.size();

        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(mp.myactivity.latitude, mp.myactivity.longitude,10);

//            for(int i=0;i<addresses.size(); i++ ) {
//                Log.e(TAG, "addresses " + i + " :" + addresses.get(i)  );
//            }

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

        //sinfo = sinfo + "\n" + addinfo;

        final TextView tv_h = new TextView(ctx);
        tv_h.setTextColor(Color.WHITE);
        tv_h.setTextSize(25);
        tv_h.setAlpha(0.9f);
        tv_h.setText(sinfo);
        tv_h.setPaintFlags(tv_h.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

        final LinearLayout llhh = new LinearLayout(ctx);
        llhh.setOrientation(LinearLayout.HORIZONTAL);
        llhh.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        llhh.setVerticalGravity(Gravity.TOP);
        llhh.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        llhh.addView(tv_h);

        final TextView tv_a = new TextView(ctx);
        tv_a.setTextColor(Color.WHITE);
        tv_a.setTextSize(25);
        tv_a.setAlpha(0.9f);
        tv_a.setText(addinfo);
        tv_a.setPaintFlags(tv_h.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

        final LinearLayout llhh2 = new LinearLayout(ctx);
        llhh2.setOrientation(LinearLayout.HORIZONTAL);
        llhh2.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        llhh2.setVerticalGravity(Gravity.CENTER_VERTICAL);

        FrameLayout.LayoutParams floparams2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        floparams2.setMargins(0,150,0,0);
        llhh2.setLayoutParams(floparams2);
        llhh2.addView(tv_a);


        final TextView tv_t = new TextView(ctx);
        tv_t.setTextColor(Color.WHITE);
        tv_t.setTextSize(23);
        tv_t.setAlpha(0.8f);
        String tv_t_str = inx_str +"";
        tv_t.setText(tv_t_str);
        tv_t.setPaintFlags(tv_t.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);

        final LinearLayout llh2 = new LinearLayout(ctx);
        llh2.setOrientation(LinearLayout.HORIZONTAL);
        llh2.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        llh2.setVerticalGravity(Gravity.BOTTOM);
        llh2.addView(tv_t);

        flo.addView(mImageView);
        flo.addView(llh2);
        flo.addView(llhh);
        flo.addView(llhh2);
        flo.addView(ll09);
        ll.addView(flo);

//        final TextView tv  = new TextView(ctx); tv.setText(inx_str); tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        ll.addView(tv);
//        final TextView tv2 = new TextView(ctx); tv2.setText(mp.picname);
//        ll.addView(tv2); tv2.setGravity(Gravity.CENTER_HORIZONTAL);

        alertDialog.setView(ll);

        mImageView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (scaleType == ImageView.ScaleType.FIT_START) {
                    scaleType = ImageView.ScaleType.CENTER;
                    Log.e(TAG,"ImageView.ScaleType.CENTER");
                } else if (scaleType == ImageView.ScaleType.CENTER) {
                    scaleType = ImageView.ScaleType.MATRIX;
                    Log.e(TAG,"ImageView.ScaleType.MATRIX");
                } else if (scaleType == ImageView.ScaleType.MATRIX) {
                  scaleType = ImageView.ScaleType.CENTER_INSIDE;
                  Log.e(TAG,"ImageView.ScaleType.CENTER_INSIDE");
                } else if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                    scaleType = ImageView.ScaleType.FIT_XY;
                    Log.e(TAG,"ImageView.ScaleType.FIT_XY");
                } else if (scaleType == ImageView.ScaleType.FIT_XY) {
                    scaleType = ImageView.ScaleType.CENTER_INSIDE;
                     Log.e(TAG,"ImageView.ScaleType.CENTER_INSIDE");
                } else if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
                    scaleType = ImageView.ScaleType.FIT_START;
                    Log.e(TAG,"ImageView.ScaleType.FIT_START");
            }
                mImageView.setScaleType(scaleType);
            }
        });

        imbt_prev.setOnClickListener(new View.OnClickListener(){
            public void onClick (View view) {
                if(position>0 && (position) < myPictureList.size() ) {
                    position--;
                    myPicture mp2 = myPictureList.get(position);
                    _alertDialog.setTitle(mp2.picname);
                    try {
//                        Bitmap bmp = BitmapFactory.decodeFile(mp2.filepath);
//                        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
                        //if(bmp.getWidth() > bmp.getHeight()) mImageView.setRotation(90);
                        Bitmap bmp3 = PhotoUtil.getPreview(mp2.filepath, 1400);
                        mImageView.setImageBitmap(bmp3);

                        LatLng startpos = RunningActivity.getCurLatLng();
                        String sinfo = null;
                        if(startpos != null) {
                            CalDistance cd = new CalDistance(mp2.myactivity.latitude, mp2.myactivity.longitude, startpos.latitude, startpos.longitude);
                            double minDist = cd.getDistance();
                            final String _minDist = (minDist>1000)?  "" + (int)(minDist/1000) + "킬로" : "" + (int)minDist + "미터";
                            Date date = StringUtil.StringToDate1(mp2.picname);
                            String date_str = StringUtil.DateToString1(date, "MM월 dd일 HH시");
                            sinfo = "\n " + date_str + "\n  (" + _minDist + " 거리)";
                        }
                        tv_h.setText(sinfo);

                        String inx_str = "" + (position+1)  + "/" + myPictureList.size();
                        //String tv_t_str = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + mp2.picname + "\n                 (" + inx_str + ")";
                        tv_t.setText(inx_str + "\n");

                        String msg = " file   :" + mp2.picname;
                        msg+= "\n size  :" + bmp3.getByteCount();
                        msg+= "\n width :" + bmp3.getWidth();
                        msg+= "\n height:" + bmp3.getHeight();
                        Log.e(TAG, msg);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                }
            }
        });

       imbt_next.setOnClickListener(new View.OnClickListener(){
            public void onClick (View view) {
                if(position>=0 && (position+1) < myPictureList.size() ) {
                    position++;
                    myPicture mp2 = myPictureList.get(position);
                    _alertDialog.setTitle(mp2.picname);
                    try {
//                        Bitmap bmp = BitmapFactory.decodeFile(mp2.filepath);
//                        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
                        //if(bmp.getWidth() > bmp.getHeight()) mImageView.setRotation(90);
                        Bitmap bmp3 = PhotoUtil.getPreview(mp2.filepath, 1400);
                        mImageView.setImageBitmap(bmp3);

                        LatLng startpos = RunningActivity.getCurLatLng();
                        String sinfo = null;
                        if(startpos != null) {
                            CalDistance cd = new CalDistance(mp2.myactivity.latitude, mp2.myactivity.longitude, startpos.latitude, startpos.longitude);
                            double minDist = cd.getDistance();
                            final String _minDist = (minDist>1000)?  "" + (int)(minDist/1000) + "킬로" : "" + (int)minDist + "미터";
                            Date date = StringUtil.StringToDate1(mp2.picname);
                            String date_str = StringUtil.DateToString1(date, "MM월 dd일 HH시");
                            sinfo = "\n " + date_str + "\n  (" + _minDist + " 거리)";
                        }
                        tv_h.setText(sinfo);

                        String inx_str = "" + (position+1)  + "/" + myPictureList.size();
                        //String tv_t_str = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + mp2.picname + "\n                 (" + inx_str + ")";
                        tv_t.setText(inx_str + "\n");

                        String msg = " file   :" + mp2.picname;
                        msg+= "\n size  :" + bmp3.getByteCount();
                        msg+= "\n width :" + bmp3.getWidth();
                        msg+= "\n height:" + bmp3.getHeight();
                        Log.e(TAG, msg);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                }
            }
        });

        alertDialog.setNegativeButton("Back",null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }


    public static void show_myPicture(Context ctx, ImageView mImageView, myPicture mp) {
        Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
        // 1/10크기로 화면 표시 하기
        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/10, bmp.getHeight()/10, true);
        mImageView.setImageBitmap(bmp2);
        mImageView.setRotation(90);
    }

    public static ArrayList<myPicture> loadMyPictueList() {
        String fileName = myPictureMetaFilename;
        File file = new File(mediaStorageDir, fileName);

        Log.e(TAG, "My Picture Master File:" + file.toString());
        Log.e(TAG, "My Picture Master File length:" + file.length());
        Log.e(TAG, "My Picture Master File exists:" + file.exists());


        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList<myPicture> list = new ArrayList<myPicture>();
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);
            list = new ArrayList<myPicture>();
            myPicture mp=null;

            int i=0;
            do {
                try {
                    mp = (myPicture) in.readObject();
                    Log.e(TAG, "" + i + "th Picture(" + mp.toString() + ")" );

                    if(mp.myactivity.added_on == "" ) {

                        String t_cr_date = mp.picname.substring(4);
                        Date t_date = StringUtil.StringToDate(t_cr_date, "yyyy_MM_dd_HH_mm_ss");
                        String str= StringUtil.DateToString1(t_date, "yyyy년MM월dd일HH시mm분ss초");
                        mp.myactivity.added_on = str;

                        Log.e(TAG, "added on ====> " + mp.myactivity.added_on);
                    }




                    list.add(mp);
                    i++;
                }catch(Exception ex) {
                    Log.e(TAG, ex.toString());
                    ex.printStackTrace();
                    break;
                }
            } while(mp != null);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ fileName +") corrupted !!!!");
//                    file.delete();
//                    Log.e(TAG, "File ("+ fileName +") deleted  !!!!");
                }
            }catch(Exception e) {}
        }
        return list;
    }

    public void addMyPicture(myPicture mPic) {
        myPictureList.add(mPic);
    }

    public void addAndSaveMyPicture(myPicture mpic) {
        addMyPicture(mpic);
        saveMyPictueList();
    }


    public void saveMyPictueList() {
        String fileName = myPictureMetaFilename;
        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "My Picture Master File:" + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            int i=0;
            while(i < myPictureList.size()) {
                myPicture mp = myPictureList.get(i);
                out.writeObject(mp);
                i++;
            }
            out.close();
            Log.e(TAG, "\n\n saveMyPictureList() called!!\n\n");
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    /** Check if this device has a camera */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Log.e(TAG, "" + e);
            Log.e(TAG, e.toString());

        }
        return c; // returns null if camera is unavailable
    }


    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    public static String mLastPictureFilename = null;

    private static File getOutputMediaFile(int type){
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                mLastPictureFilename = null;
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            if(mLastPictureFilename != null) {
                mediaFile = new File(mLastPictureFilename);
            } else {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");
            }
            mLastPictureFilename = mediaFile.getAbsolutePath();
        } else {
            mLastPictureFilename = null;
            return null;
        }

        mLastPictureFilename = mediaFile.getAbsolutePath();
        return mediaFile;
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.e(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Log.e(TAG, "Picture Taken: " + pictureFile.getAbsolutePath());

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
        }
    };

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    public  void TakeAPicture(Camera mCamera, Location loc, EditText et) {
        mCamera.takePicture(null,null,mPicture);
        myActivity myactivity = null;

        if(loc != null) {
            myactivity = new myActivity(loc.getLatitude(), loc.getLongitude(), StringUtil.DateToString1(new Date(), "yyyy년MM월dd일HH시mm분ss초"));
        } else {
            myactivity = new myActivity(37.4992434, 127.1316329, StringUtil.DateToString1(new Date(), "yyyy년MM월dd일HH시mm분ss초"));
        }


        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mLastPictureFilename = mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg";


        myPicture mpic = new myPicture(myactivity, et.getText().toString(), mLastPictureFilename);
        addAndSaveMyPicture(mpic);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        Date now = new Date();
        final String fileName = "PIC_" + formatter.format(now);
        et.setText(fileName);
    }


    public void preview(Context ctx, Location location) {
        final Location loc = location;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        alertDialog.setTitle("Camera Preview");

        LinearLayout lllo = new LinearLayout(ctx);
        lllo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams loparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lllo.setLayoutParams(loparams);

        final FrameLayout flo = new FrameLayout(ctx);
        FrameLayout.LayoutParams floparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        flo.setLayoutParams(floparams);

//        final Button bt = new Button(ctx);
//        bt.setText("Capture");
//        bt.setWidth(15); bt.setGravity(Gravity.CENTER);

        final EditText et = new EditText(ctx);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        Date now = new Date();
        final String fileName = "PIC_" + formatter.format(now);
        et.setText(fileName);

        lllo.addView(et);
        lllo.addView(flo);
//        lllo.addView(bt);

        alertDialog.setView(lllo);

        // Create an instance of Camera
        final Camera mCamera = getCameraInstance();
        if (mCamera==null) {
            Toast.makeText(ctx, "Can't getCameraInstance()", Toast.LENGTH_LONG).show();
            return;
        }

        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(params);

        // Create our Preview view and set it as the content of our activity.
        CameraPreview mPreview = new CameraPreview(ctx, mCamera);
        flo.addView(mPreview);

        final Context _ctx = ctx;

        mPreview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TakeAPicture(mCamera, loc, et);
                Toast.makeText(_ctx, mLastPictureFilename + " created successfully", Toast.LENGTH_LONG).show();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
                Date now = new Date();
                final String fileName = "PIC_" + formatter.format(now);
                et.setText(fileName);
            }
        });

//        bt.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                TakeAPicture(mCamera, loc, et);
//                Toast.makeText(_ctx, mLastPictureFilename + " created successfully", Toast.LENGTH_LONG).show();
//            }
//        });

        alertDialog.setPositiveButton("Capture", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                TakeAPicture(mCamera, loc, et);
                Toast.makeText(_ctx, mLastPictureFilename + " created successfully", Toast.LENGTH_LONG).show();
            }
        });
        alertDialog.setNegativeButton("Cancel", null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }


    // maxImageSize = 300, Small picture
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());
        return output;
    }

    public static Bitmap getPreview(String filepath, int thumsize) {
        File image = new File(filepath);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);

        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumsize ;//Thumb size
        Bitmap bitmap =  BitmapFactory.decodeFile(image.getPath(), opts);
        if(bitmap == null) return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotated = bitmap;

        boolean  go_rotate=false;
        if(bounds.outHeight < bounds.outWidth) go_rotate = true;

        if(go_rotate) {
            try {
                rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rotated;
    }

    public static void Admin_Backup_All_Pictures(final Context context) {
        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {
                asyncDialog.setMax(myPictureList.size());

                for(int i=myPictureList.size()-1; i>0; i--) {
                    asyncDialog.setProgress(myPictureList.size() - i);
                    myPicture mp = myPictureList.get(i);
                    File _src = new File(mp.filepath);
                    if(_src == null ) continue;
                    if(!_src.exists()) continue;

                    if(!backupDir.exists()) backupDir.mkdir();
                    File _tar = new File(backupDir, _src.getName());
                    Log.e(TAG, "Backup " + _src.getAbsolutePath() + " To " + _tar.getAbsolutePath());
                    try {
                        FileUtils.copyFileUsingFileStreams(_src, _tar);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
                asyncDialog.setMax(myPictureList.size());
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                File _src = new File(mediaStorageDir, myPictureMetaFilename);
                File _tar = new File(backupDir, myPictureMetaFilename);

                try {
                    FileUtils.copyFileUsingFileStreams(_src, _tar);
                }catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);

                Toast.makeText(context, "Backup Completed !!", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public static void Admin_Remove_Missed_Pictures(final Context context) {
        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {
                asyncDialog.setMax(myPictureList.size());
                for(int i=myPictureList.size()-1; i>0; i--) {
                    asyncDialog.setProgress(i);
                    myPicture mp = myPictureList.get(i);
                    File _file = new File(mp.filepath);
                    if(_file == null ) {

                        Log.e(TAG, "ERR] null " + mp.filepath + " not found!");
                        myPictureList.remove(i);
                        continue;
                    }

                    if(! _file.exists()) {
                        Log.e(TAG, "ERR] ???? " + mp.filepath + " not found!");
                        myPictureList.remove(i);
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                PhotoUtil pu = new PhotoUtil();
                pu.saveMyPictueList();

                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    public static void Admin_Remove_Currupted_Pictues(final Context context) {
        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {
                asyncDialog.setMax(myPictureList.size());
                for(int i=myPictureList.size()-1; i>0; i--) {
                    asyncDialog.setProgress(myPictureList.size() - i);
                    myPicture mp = myPictureList.get(i);
                    Bitmap bmp3 = PhotoUtil.getPreview(mp.filepath, 1400);
                    if (bmp3 == null) myPictureList.remove(i);
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                PhotoUtil pu = new PhotoUtil();
                pu.saveMyPictueList();

                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    public static void Admin_Add_Unregistered_Pictures(final Context context, final File folder) {


        Log.e(TAG+"ADMIN" , "Folder: "+folder.getAbsolutePath());

        if(!folder.exists()) {
            Log.e(TAG+"ADMIN" , "Folder: "+folder.getAbsolutePath() + " not found!");
            return;
        }

        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".jpg");
            }
        };

        File[] flist  = folder.listFiles(fnf);

        if(flist==null) {
            Log.e(TAG+"ADMIN" , "flist is null");
            return;
        }

        Log.e(TAG+"ADMIN" , "Folder size: "+flist.length);

        File flist_tmp[]  = new File[flist.length];
        for(int i=0;i<flist.length;i++ ) {
            String fname = flist[i].getName();
            if(!fname.startsWith("IMG_")) {
                File rfile = new File(mediaStorageDir, "IMG_" + flist[i].getName());
                boolean ismoved = flist[i].renameTo(rfile);
                if(ismoved) {
                    Log.e(TAG, "(성공)Renamed to : " + rfile.getAbsolutePath());
                }else {
                    Log.e(TAG, "(실패)Renamed to : " + rfile.getAbsolutePath());
                }

                flist_tmp[i] = rfile;
            } else flist_tmp[i] = flist[i];
        }

        final String folderlist[] = new String[flist.length];

        for(int i=0;i<folderlist.length;i++) {
            folderlist[i] = flist_tmp[i].getName();
        }


        // myPictureList에  PIC 이중 저장된 객체 삭제.
        String [] mylist_org = null;
        for(int x=0;x<myPictureList.size();x++) {
            mylist_org = new String[myPictureList.size()];
            myPicture mp = myPictureList.get(x);
            mylist_org[x] = mp.filepath.substring(mp.filepath.indexOf("IMG_"));
        }

        final String mylist[]  = mylist_org;

        for(int i=0;i<mylist.length;i++) {
            // rebuild


            String mylist2[] = new String[myPictureList.size()];
            for(int x=0;x<myPictureList.size();x++) {
                myPicture mp = myPictureList.get(x);
                mylist2[x] = mp.filepath.substring(mp.filepath.indexOf("IMG_"));
            }

            String src = null;
            if(i< mylist2.length) {
                src = mylist2[i];
            } else {
                continue;
            }

            // dupcheck
            for(int j=mylist2.length-1; j>i; j--)  {

                //Log.e(TAG, "i=" + i + "  j=" + j + " mylist.length = " + mylist.length + " mPictureList.size()= " + myPictureList.size());

                String tar = mylist2[j];
                //Log.e(TAG, "src=" + src + "  tar=" + tar);

                if( src.equalsIgnoreCase(tar) ) {
                    Log.e(TAG, "DUPPPPP Pic !! " + i + " =" + j  + " " + mylist[i]) ;
                    myPictureList.remove(j);
                }
            }
        }

        final String[] piclist = new String[myPictureList.size()];
        for(int i=0;i<myPictureList.size();i++) {
            myPicture mp = myPictureList.get(i);
            piclist[i] = mp.filepath.substring(mp.filepath.indexOf("IMG_"));
        }

        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(Void... voids) {

                // --

                for(int i=0;i<folderlist.length;i++) {
                    asyncDialog.setProgress(i);
                    boolean found = false;
                    for(int j=0;j<piclist.length;j++) {

                        if(folderlist[i].substring(4).equalsIgnoreCase(piclist[j].substring(4))) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        //파일의 포맷에서 데이트포맷을 찾음.

                        String t_cr_date = folderlist[i].substring(4, 19);
                        Date t_date = StringUtil.StringToDate(t_cr_date, "yyyyMMdd_HHmmss");

                        String p_name = "PIC_" + StringUtil.DateToString1(t_date, "yyyy_MM_dd_HH_mm_ss");  // 저장될 대상
                        String str= StringUtil.DateToString1(t_date, "yyyy년MM월dd일HH시mm분ss초");


                        long time_gap[] = new long [piclist.length];
                        for(int q=0;q<piclist.length;q++) {
                            Date t2_date = StringUtil.StringToDate(piclist[q].substring(4,19),"yyyyMMdd_HHmmss");

                            time_gap[q] =  t_date.getTime() - t2_date.getTime();
                            if(time_gap[q] < 0) time_gap[q] = time_gap[q] * -1;
                        }

                        int m_adjacent_pos = -1;
                        long m_tg_min = Long.MAX_VALUE;
                        for(int q=0;q<piclist.length;q++) {
                            if(m_tg_min > time_gap[q]) {
                                m_tg_min = time_gap[q];
                                m_adjacent_pos = q;
                            }
                        }

                        myPicture myPictureFound = myPictureList.get(m_adjacent_pos);
                        myActivity ma = new myActivity(myPictureFound.myactivity.latitude, myPictureFound.myactivity.longitude, str);
                        File f = new File(folderlist[i]);

                        myPicture mp = new myPicture(ma, p_name, folder.getAbsolutePath() + "/"+ folderlist[i]);
                        myPictureList.add(mp);
                        Log.e(TAG, "Not Found file  (" + folderlist[i] + ") added a new mp -->" + mp.toString() );
                    } else {
                        Log.e(TAG, "Found " + folderlist[i]);
                    }
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.setMax(folderlist.length);
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                PhotoUtil pu = new PhotoUtil();
                pu.saveMyPictueList();

                Log.e(TAG+"ADMIN" , "REBUILD DONE !!!!! ");


                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
            }
        }.execute();

    }


}


