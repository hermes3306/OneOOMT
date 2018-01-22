package com.joonho.myway.util;

import java.io.File;
import java.util.Date;

/**
 * Created by nice9uy on 18. 1. 10.
 */

public class Config {
    public static String        _default_ext                    = ".ser2";

    public static Date          _last_save_point                = null;
    public static String        _last_save_fname                = null;
    public static String        _filename_fmt                   ="yyyyMMdd(E)HHmmss";

    public static boolean       _delete_file_with_same_start    = true;
    public static boolean       _trash_after_dododo             = false;

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

    public static final long    _timer_period                     = 5000; // 10 sec
    public static final long    _timer_delay                      = 5000; // 5 sec
    public static boolean       _driving_mode                       = false;
    public static float         _myzoom                              =14f;

    public static String get_filename() {
        return StringUtil.DateToString(new Date(), _filename_fmt) + _default_ext;
    }

    public static String getAbsolutePath(String fname) {
        File file = new File(MyActivityUtil.getMediaStorageDirectory(), fname);
        return file.getAbsolutePath();
    }


}
