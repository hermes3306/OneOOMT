package com.joonho.runme.util;

import org.json.JSONObject;

/**
 * Created by jhpark on 17. 12. 28.
 */

public class JSONUtil {
    public static String httpParser(String url) {
        String str = JSONUtil.httpParser(url);
        return str;
    }

    public static void main(String args[]) {

        try {
            String json = httpParser("http://180.69.217.73:8080/OneOOMT/list.jsp"));
            System.out.println(json);
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
}
