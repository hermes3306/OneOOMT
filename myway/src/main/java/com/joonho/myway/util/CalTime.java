package com.joonho.myway.util;

import com.joonho.myway.MyActivity;

/**
 * Created by joonhopark on 2017. 12. 21..
 */

public class CalTime {
    public long bef, cur;
    public static String TAG = "CalTime";

    public CalTime(long bef, long cur) {
        this.bef = bef;
        this.cur = cur;
    }

    public CalTime(MyActivity bef, MyActivity cur) {
        this.bef = StringUtil.StringToDate(bef.added_on, "yyyy년MM월dd일_HH시mm분ss초").getTime();
        this.cur = StringUtil.StringToDate(cur.added_on, "yyyy년MM월dd일_HH시mm분ss초").getTime();
    }

    public long getBef() {
        return bef;
    }

    public void setBef(long bef) {
        this.bef = bef;
    }

    public long getCur() {
        return cur;
    }

    public void setCur(long cur) {
        this.cur = cur;
    }

    public long getElapsed() {
        return Math.abs(cur - bef);
    }

    public float getElapsedSec() {
        return (getElapsed())/1000.0f;
    }

    public float getElapsedMin() {
        return getElapsedSec()/60.0f;
    }

    public float getElapsedHour() {
        return getElapsedMin()/60.f;
    }

    public String getElapsedSecStr() {
        return String.format("%.0f초", getElapsedSec());
    }

    public String getElapsedMinStr() {
        return String.format("%.0f분", getElapsedMin());
    }

    public String getElapsedHourStr() {
        return String.format("%.0f시간%d분", getElapsedHour(), (int)getElapsedMin() - (int)getElapsedHour() * 60 );
    }


}
