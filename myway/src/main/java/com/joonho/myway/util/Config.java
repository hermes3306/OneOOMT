package com.joonho.myway.util;

import android.os.Environment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.util.Date;

/**
 * Created by nice9uy on 18. 1. 10.
 */

public class Config {
    public static String        _default_ext                    = ".ser2";
    public static File          mediaStorageDir                 =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "OneOOMT");
    public static String        _uploadURL                      =
            "http://180.69.217.73:8080/OneOOMT/upload2";
    public static String        _listURL                        =
            "http://180.69.217.73:81/OneOOMT/filelist2.php";

    public static Date          _last_save_point                = null;
    public static String        _last_save_fname                = null;
    public static String        _filename_fmt                   ="yyyyMMdd_HHmmss";

    public static boolean       _delete_file_with_same_start    = true;
    public static boolean       _trash_after_dododo             = false;
    public static int           _dododo_day                     = 2;

    public static int           _save_interval                  = 1;
    public static final int     _SAVE_INTERVAL_MININUTE         = 0;
    public static final int     _SAVE_INTERVAL_10MINUTEES       = 1;
    public static final int     _SAVE_INTERVAL_30MINUTEES       = 2;
    public static final int     _SAVE_INTERVAL_HOUR             = 3;
    public static final int     _SAVE_INTERVAL_6HOURS           = 4;
    public static final int     _SAVE_INTERVAL_12HOURS          = 5;
    public static final int     _SAVE_INTERVAL_DAY              = 6;

    public static final long    _SECOND                         = 1000 ;
    public static final long    _30SECONDS                      = _SECOND * 30;
    public static final long    _MINUTE                         = _SECOND * 60;
    public static final long    _10MINUTES                      = _MINUTE * 10;
    public static final long    _30MINUTES                      = _MINUTE * 30;
    public static final long    _HOUR                           = _MINUTE * 60;
    public static final long    _6HOURs                         = _HOUR * 6;
    public static final long    _12HOURS                        = _HOUR * 12;
    public static final long    _DAY                            = _HOUR * 24;

    public static final int     _location_interval                = 1000; // 1 sec
    public static final long    _timer_period                     = 1000; // 1 sec
    public static final long    _timer_delay                      = 1000; // 1 sec
    public static boolean       _driving_mode                     = false;
    public static float         _myzoom                           = 15.0f;
    public static float         _marker_color                     = BitmapDescriptorFactory.HUE_RED;

    public static String get_filename() {
        return StringUtil.DateToString(new Date(), _filename_fmt) + _default_ext;
    }

    public static String getAbsolutePath(String fname) {
        File file = new File(MyActivityUtil.getMediaStorageDirectory(), fname);
        return file.getAbsolutePath();
    }


}
