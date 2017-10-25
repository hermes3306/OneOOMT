package com.joonho.oneoomt.util;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.joonho.oneoomt.file.myActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.joonho.oneoomt.RunningActivity.mCurLoc;
import static com.joonho.oneoomt.RunningActivity.mLatLngList;
import static com.joonho.oneoomt.RunningActivity.mLocTime;

/**
 * Created by user on 2017-10-25.
 */

public class ActivityUtil {
    public static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    public static String TAG = "ActivityUtil";
    public static void serialize() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy년MM월dd일_HH시mm분ss초", Locale.KOREA);
        Date now = new Date();

        String fileName = formatter.format(now) + ".ser";
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);

        Log.e(TAG, "ActivityFileName to be written: " + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);


            // 타임저장부분을 처리 필요함. 현재는 longtype을 그대로 저장함.
            //


            for(int i=0;i<mLatLngList.size();i++) {
                Date t_date = new Date((long)mLocTime.get(i));
                String addon = StringUtil.DateToString1(t_date, "yyyy년MM월dd일_HH시mm분ss초");
                myActivity ma = new myActivity(mLatLngList.get(i).latitude, mLatLngList.get(i).longitude, addon);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static void deserialize() {

    }
}
