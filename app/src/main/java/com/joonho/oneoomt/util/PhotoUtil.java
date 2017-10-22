package com.joonho.oneoomt.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by joonhopark on 2017. 9. 17..
 */

public class PhotoUtil {
    public static File mediaStorageDir =  null;

    public static String TAG = "PhotoUtil";
    public String myPictureMetaFilename = "";
    public static ArrayList<myPicture> myPictureList = new ArrayList<myPicture> ();
    public static int position=0;

    static {
//        mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OneOOMT");
        myPictureList = loadMyPictueList();

        // 초기화
        //myPictureList = new ArrayList<myPicture>();
    }

    public static int picsize = 100;
    public static void validatePictures() {
        if(myPictureList.size()==0) return;
        Boolean dl[] = new Boolean[myPictureList.size()];
        for(int i=0;i<myPictureList.size();i++) {
            myPicture mp = myPictureList.get(i);
            Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
            if(bmp == null){
                dl[i] = true;
            }else {
                dl[i] = false;
            }
        }

        for(int i=myPictureList.size()-1; i>=0; i--) {
            if(dl[i]) myPictureList.remove(i);
        }
    }

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
        Bitmap bmp = BitmapFactory.decodeFile(mp.filepath);
        if(bmp == null){
            Toast.makeText(ctx, "Error while decoding image file " + mp.filepath, Toast.LENGTH_LONG).show();
            return;
        }

        final AlertDialog.Builder _alertDialog = alertDialog;
        alertDialog.setTitle(mp.picname);
//        String msg = " file   :" + mp.picname;
//        msg+= "\n size  :" + bmp.getByteCount();
//        msg+= "\n width :" + bmp.getWidth();
//        msg+= "\n height:" + bmp.getHeight();
//
//        alertDialog.setMessage(msg);

        Log.e(TAG,"file size:" + bmp.getByteCount());
        Log.e(TAG,"width    :" + bmp.getWidth());
        Log.e(TAG,"height   :" + bmp.getHeight());

        final LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.VERTICAL);


        Button bt_prev = new Button(ctx);
        bt_prev.setText("Prev");
        Button bt_next = new Button(ctx);
        bt_next.setText("Next");

        final ImageView mImageView =  new ImageView(ctx);
        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
        mImageView.setImageBitmap(bmp2);
        //mImageView.setRotation(90);

        final LinearLayout llh = new LinearLayout(ctx);
        llh.setOrientation(LinearLayout.HORIZONTAL);
        llh.addView(bt_prev);
        llh.addView(bt_next);
        llh.setGravity(Gravity.CENTER);

        ll.addView(mImageView);
        String inx_str = "" + (index+1) + "/" + myPictureList.size();
        final TextView tv  = new TextView(ctx); tv.setText(inx_str); tv.setGravity(Gravity.CENTER);
        final TextView tv2 = new TextView(ctx); tv2.setText(mp.picname);
        ll.addView(tv2); tv2.setGravity(Gravity.CENTER);
        final TextView tv3 = new TextView(ctx); tv3.setText("");  // space
        ll.addView(tv3);

        ll.addView(tv);
        ll.addView(llh);
        alertDialog.setView(ll);

        mImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if(position>=0 && (position+1) < myPictureList.size() ) {
//
//                    myPicture mp2 = myPictureList.get(position);
//                    _alertDialog.setTitle(mp2.picname);
//                    try {
//                        Bitmap bmp = BitmapFactory.decodeFile(mp2.filepath);
//                        //Bitmap bmp2 = scaleBitmap(bmp,500,600);
//
//                        picsize = picsize + (int)(picsize * 0.1);
//                        Bitmap bmp3 = scaleDown(bmp, picsize, true);
//                        Toast.makeText(cur_context, "" + picsize, Toast.LENGTH_SHORT).show();
//
//                        mImageView.setImageBitmap(bmp3);
//                        mImageView.setRotation(90);
//
//                        String inx_str = "" + (position+1)  + "/" + myPictureList.size();
//                        tv.setText(inx_str);
//                        tv2.setText(mp2.picname);
//
//                        String msg = " file   :" + mp2.picname;
//                        msg+= "\n size  :" + bmp3.getByteCount();
//                        msg+= "\n width :" + bmp3.getWidth();
//                        msg+= "\n height:" + bmp3.getHeight();
//                        Log.e(TAG, msg);
//                    }catch(Exception e) {
//                        e.printStackTrace();
//                        Log.e(TAG,e.toString());
//                    }
//                }
            }}
        );

        bt_prev.setOnClickListener(new View.OnClickListener(){
            public void onClick (View view) {
                if(position>0 && (position) < myPictureList.size() ) {
                    position--;
                    myPicture mp2 = myPictureList.get(position);
                    _alertDialog.setTitle(mp2.picname);
                    try {
                        Bitmap bmp = BitmapFactory.decodeFile(mp2.filepath);
                        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
                        mImageView.setImageBitmap(bmp2);
                        mImageView.setRotation(90);

                        String inx_str = "" + (position+1)  + "/" + myPictureList.size();
                        tv.setText(inx_str);
                        tv2.setText(mp2.picname);

                        String msg = " file   :" + mp2.picname;
                        msg+= "\n size  :" + bmp.getByteCount();
                        msg+= "\n width :" + bmp.getWidth();
                        msg+= "\n height:" + bmp.getHeight();
                        Log.e(TAG, msg);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,e.toString());
                    }
                }
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener(){
            public void onClick (View view) {
                if(position>=0 && (position+1) < myPictureList.size() ) {
                    position++;
                    myPicture mp2 = myPictureList.get(position);
                    _alertDialog.setTitle(mp2.picname);
                    try {
                        Bitmap bmp = BitmapFactory.decodeFile(mp2.filepath);
                        Bitmap bmp2 = bmp.createScaledBitmap(bmp, bmp.getWidth()/2, bmp.getHeight()/2, true);
                        mImageView.setImageBitmap(bmp2);
                        mImageView.setRotation(90);

                        String inx_str = "" + (position+1)  + "/" + myPictureList.size();
                        tv.setText(inx_str);
                        tv2.setText(mp2.picname);

                        String msg = " file   :" + mp2.picname;
                        msg+= "\n size  :" + bmp.getByteCount();
                        msg+= "\n width :" + bmp.getWidth();
                        msg+= "\n height:" + bmp.getHeight();
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
        String fileName = "myPicture.master";
        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "My Picture Master File:" + file.toString());



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
        String fileName = "myPicture.master";
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
                Log.e(TAG, "" + i + "th Picture(" + mp.toString() + ") OK!" );
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
}
