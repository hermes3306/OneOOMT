package com.joonho.oneoomt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.provider.MediaStore.AUTHORITY;

public class ViewCameraPicActivity extends AppCompatActivity {
    private String TAG = "ViewCameraPicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_camera_pic);
        setPic2();
        btn_event();
    }

    static int _cur_pos = 0;

    public void btn_event() {
        final Button button1 = (Button) findViewById(R.id.bt_prev);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = getIntent();
                String fnamelist[] = intent.getStringArrayExtra("fnamelist");
                if( (_cur_pos) > 0 )  _cur_pos--; else return;
                setPic3(fnamelist[_cur_pos]);
            }
        });

        final ImageButton ib = (ImageButton) findViewById(R.id.ib_delete);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String fname = intent.getStringExtra("fname");
                File f = new File(fname);
                f.delete();
                Toast.makeText(getApplicationContext(),"" + fname + " deleted!", Toast.LENGTH_LONG).show();
            }
        });

        final Button button2 = (Button) findViewById(R.id.bt_next);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = getIntent();
                String fnamelist[] = intent.getStringArrayExtra("fnamelist");
                if( (_cur_pos+1) < fnamelist.length )  _cur_pos++; else return;
                setPic3(fnamelist[_cur_pos]);
            }
        });
    }

    private void setPic3(String filepath) {
        Bitmap capturebmp = BitmapFactory.decodeFile(filepath);
        final String _filepath = filepath;
        ImageView mImageView = (ImageView) findViewById(R.id.imageview_camera_pic);
        mImageView.setImageBitmap(capturebmp);
        mImageView.setRotation(90);


        // 매우 중요한 파일프로바이더 사용하는 방법
        mImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                File fp = new File(_filepath);

                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File f = new File(storageDir, fp.getName());

                // ******* Uri *************
                final Uri uri = FileProvider.getUriForFile(ViewCameraPicActivity.this, "com.joonho.oneoomt.fileprovider", f);


                //final Intent intent = new Intent(Intent.ACTION_SEND);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
            }
        });


        Intent intent = getIntent();
        String fnamelist[] = intent.getStringArrayExtra("fnamelist");
        TextView tv = (TextView)findViewById(R.id.textview_head);
        tv.setText("" + (_cur_pos+1) + "/" + fnamelist.length);
    }

    private void setPic2() {
        Intent intent = getIntent();
        String fname = intent.getStringExtra("fname");
        _cur_pos  = intent.getIntExtra("index",0);
        setPic3(fname);
    }

    private void setPic() {
        Intent intent = getIntent();
        String fname = intent.getStringExtra("fname");
        _cur_pos  = intent.getIntExtra("index",0);

        Bitmap capturebmp = null;
        ImageView mImageView = (ImageView)findViewById(R.id.imageview_camera_pic);

        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();


        if(targetW==0) targetW = mImageView.getMaxWidth();
        if(targetH==0) targetH = mImageView.getMaxHeight();

        Log.e(TAG, "mImageView.getWidth()=" + targetW);
        Log.e(TAG, "mImageView.getHight()=" + targetH);
        Log.e(TAG, "mImageView.getMaxWidth()=" + mImageView.getMaxWidth());
        Log.e(TAG, "mImageView.getMaxHight()=" + mImageView.getMaxHeight());

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fname, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.e(TAG, "photoW=" + photoW);
        Log.e(TAG, "photoH=" + photoH);


        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        Log.e(TAG, "scaleFactor=" + scaleFactor);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(fname, bmOptions);
        mImageView.setImageBitmap(bitmap);
        mImageView.setRotation(90);
    }
}
