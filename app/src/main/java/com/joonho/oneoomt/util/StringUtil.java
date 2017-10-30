package com.joonho.oneoomt.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017-10-22.
 */

public class StringUtil {
    public static Date StringToDate1 (String str_date)  {  // PIC_2017_10_21_09_49_51
        DateFormat formatter ;
        Date date ;

        try {
            formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            date = (Date)formatter.parse(str_date.substring(4));
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static Date StringToDate (String str_date, String fmt)  {
        DateFormat formatter ;
        Date date ;

        try {
            formatter = new SimpleDateFormat(fmt);
            date = (Date)formatter.parse(str_date);
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    public static String getMinPerKM(double dist_kilo, long elapsed) {
        double elaped_min = elapsed / 60f;
        double minpk = (double) (elaped_min / dist_kilo);
        String minpk_str = null;

        int minpk_hour = (int)(minpk/3600f);
        int minpk_min =  (int)((minpk - minpk_hour*3600f) /60f);
        int minpk_sec =  (int)(minpk - minpk_hour*3600f - minpk_min * 60f);

        if(Double.isInfinite(minpk) || Double.isNaN(minpk)) minpk_str = "--:--";
        else if(minpk_hour != 0)          minpk_str = String.format("%2d:%02d:%02d", minpk_hour, minpk_min, minpk_sec);
        else                        minpk_str = String.format("%2d:%02d", minpk_min, minpk_sec);
        return minpk_str;
    }

    public static String getElapsedTimeStr(long elapsed) {
        double elaped_min = elapsed / 60f;
        double minpk = elaped_min;
        String minpk_str = null;

        int minpk_hour = (int)(minpk/3600f);
        int minpk_min =  (int)((minpk - minpk_hour*3600f) /60f);
        int minpk_sec =  (int)(minpk - minpk_hour*3600f - minpk_min * 60f);

        if(Double.isInfinite(minpk) || Double.isNaN(minpk)) minpk_str = "--:--";
        else if(minpk_hour != 0)          minpk_str = String.format("%2d:%02d:%02d", minpk_hour, minpk_min, minpk_sec);
        else                        minpk_str = String.format("%2d:%02d", minpk_min, minpk_sec);
        return minpk_str;
    }

    public static String DateToString1(Date date, String format) { // eg) format = "yyyy/MM/dd HH:mm:ss"
        String dformat = format;
        if (format == null) dformat = "yyyy/MM/dd HH:mm:ss";

        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(dformat);
        String date_to_string = dateformatyyyyMMdd.format(date);
        return date_to_string;
    }


    public static String Duration(Date start_date, Date end_date) {
        long endTime, startTime;
        startTime = start_date.getTime();
        endTime = end_date.getTime();
        long duration = endTime - startTime;

        long dur_sec = duration / 1000;
        long duration_r;

        int dur_hour_Int = (int)(dur_sec/3600);
        duration_r = dur_sec - (long)(dur_hour_Int) * 60 * 60;
        int dur_min_Int = (int)(duration_r / 60);
        duration_r = duration_r  - (long)(dur_min_Int) * 60;
        int dur_sec_Int = (int)(duration_r);

        String dur_str="";
        if(dur_hour_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_hour_Int + ":";

        if(dur_min_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_min_Int + ":";

        if(dur_sec_Int < 10) dur_str += "0";
        dur_str = dur_str + dur_sec_Int;

        return(dur_str);
    }



}
