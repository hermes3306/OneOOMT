package com.joonho.oneoomt.file;

import java.io.Serializable;

/**
 * Created by user on 2017-09-12.
 */
// method를 추가하여도 시리얼 진행시 문제가 발생함.
public class myActivity implements Serializable {
    public double latitude;
    public double longitude;
    public String added_on;

    public myActivity(double l1, double l2, String a) {
        this.latitude = l1;
        this.longitude = l2;
        this.added_on = a;
    }

    public String toString() {
        return "("+ latitude+ "," + longitude+ "," + added_on + ")";
    }
}
