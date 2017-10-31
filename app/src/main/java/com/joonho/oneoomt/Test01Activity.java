package com.joonho.oneoomt;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Test01Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test01);

        new AsyncTask<Void,Void,Void>(){
            String strUrl = null;
            URL Url = null;
            String strCookie = null;
            String result;
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(Test01Activity.this,
                        "ProgressDialog",
                        "이제 "+ 60 + " 초 동안 기다려 주세요.");
                        strUrl = "http://www.naver.com"; //탐색하고 싶은 URL이다.
            }

            @Override
            protected Void doInBackground(Void... voids) {
                publishProgress();
                try{
                    Url = new URL(strUrl); // URL화 한다.
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
                    conn.setRequestMethod("GET"); // get방식 통신
                    conn.setDoOutput(true); // 쓰기모드 지정
                    conn.setDoInput(true); // 읽기모드 지정
                    conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
                    conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

                    strCookie = conn.getHeaderField("Set-Cookie"); //쿠키데이터 보관


                    InputStream is = conn.getInputStream(); //input스트림 개방

                    StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8")); //문자열 셋 세팅
                    String line;

                    while ((line = reader.readLine()) != null) {

                        try {
                            Thread.sleep(1000);
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                        builder.append(line+ "\n");
                    }

                    result = builder.toString();

                }catch(MalformedURLException | ProtocolException exception) {
                    exception.printStackTrace();
                }catch(IOException io){
                    io.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                System.out.println(result);
            }
        }.execute();


    }
}
