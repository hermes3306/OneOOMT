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


    public static String DateToString1(Date date, String format) { // eg) format = "yyyy/MM/dd HH:mm:ss"
        String dformat = format;
        if (format == null) dformat = "yyyy/MM/dd HH:mm:ss";

        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat(dformat);
        String date_to_string = dateformatyyyyMMdd.format(date);
        return date_to_string;
    }






}
