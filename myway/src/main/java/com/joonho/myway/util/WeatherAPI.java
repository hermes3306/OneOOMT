package com.joonho.myway.util;

/**
 * Created by user on 2017-12-08.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class WeatherAPI {
    public static String TAG = "WeatherAPI";
    public class Weather {
        double lat;
        double ion;
        int temprature;
        int cloudy;
        String city;

        public void setLat(double lat){ this.lat = lat;}
        public void setIon(double ion){ this.ion = ion;}
        public void setTemprature(int t){ this.temprature = t;}
        public void setCloudy(int cloudy){ this.cloudy = cloudy;}
        public void setCity(String city){ this.city = city;}

        public double getLat(){ return lat;}
        public double getIon() { return ion;}
        public int getTemprature() { return temprature;}
        public int getCloudy() { return cloudy; }
        public String getCity() { return city; }
    }

    public class myWeather {
        public myWeather() {
            this.temp = 0.0f;
        }

        public myWeather(double lat, double lon, String country, Date sunrize, Date sunset, double temp, int pressure, int humidity, double temp_min, double temp_max, double wind_speed, int clouds, String name) {
            this.lat = lat;
            this.lon = lon;
            this.country = country;
            this.sunrize = sunrize;
            this.sunset = sunset;
            this.temp = temp;
            this.pressure = pressure;
            this.humidity = humidity;
            this.temp_min = temp_min;
            this.temp_max = temp_max;
            this.wind_speed = wind_speed;
            this.clouds = clouds;
            this.name = name;
        }

        double lat;
        double lon;
        String country;
        Date sunrize;
        Date sunset;
        double temp;
        int pressure;
        int humidity;
        double temp_min;
        double temp_max;
        double wind_speed;
        int clouds;
        String name;

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public String getCountry() {
            return country;
        }

        public Date getSunrize() {
            return sunrize;
        }

        public Date getSunset() {
            return sunset;
        }

        public double getTemp() {
            return temp;
        }

        public int getPressure() {
            return pressure;
        }

        public int getHumidity() {
            return humidity;
        }

        public double getTemp_min() {
            return temp_min;
        }

        public double getTemp_max() {
            return temp_max;
        }

        public double getWind_speed() {
            return wind_speed;
        }

        public int getClouds() {
            return clouds;
        }

        public String getName() {
            return name;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setSunrize(Date sunrize) {
            this.sunrize = sunrize;
        }

        public void setSunset(Date sunset) {
            this.sunset = sunset;
        }

        public void setTemp(double temp) {
            this.temp = temp - 273.15;  // 절대온도 변환 필
        }

        public void setPressure(int pressure) {
            this.pressure = pressure;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public void setTemp_min(double temp_min) {
            this.temp_min = temp_min  - 273.15;
        }

        public void setTemp_max(double temp_max) {
            this.temp_max = temp_max  - 273.15;
        }

        public void setWind_speed(double wind_speed) {
            this.wind_speed = wind_speed;
        }

        public void setClouds(int clouds) {
            this.clouds = clouds;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "myWeather{" +
                    "lat=" + lat +
                    ", lon=" + lon +
                    ", country='" + country + '\'' +
                    ", sunrize=" + sunrize +
                    ", sunset=" + sunset +
                    ", temp=" + temp +
                    ", pressure=" + pressure +
                    ", humidity=" + humidity +
                    ", temp_min=" + temp_min +
                    ", temp_max=" + temp_max +
                    ", wind_speed=" + wind_speed +
                    ", clouds=" + clouds +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    final static String openWeatherURL = "http://api.openweathermap.org/data/2.5/weather";

    public myWeather getMyWeather(double lat, double lon) {
        String lats = String.format("%.6f", lat);
        String lons = String.format("%.6f", lon);
        myWeather mw = new myWeather();
        String urlString = openWeatherURL + "?lat="+lats+"&lon="+lons +"&APPID=967b32afdae442657e1041e342e75fb5";
        Log.e("openWeatherURL",urlString);
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));
            mw=parseJSON2(json);
        }catch(MalformedURLException e){
            Log.e(TAG, e.toString());
            Log.e(TAG, "ERR] cannot get the weather information("+lat+","+lon+")");
            Log.e(TAG,"Malformed URL");
            e.printStackTrace();

        }catch(JSONException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "ERR] cannot get the weather information("+lat+","+lon+")");
            Log.e(TAG,"JSON parsing error");
            e.printStackTrace();

        }catch(IOException e){
            Log.e(TAG, e.toString());
            Log.e(TAG, "ERR] cannot get the weather information("+lat+","+lon+")");
            Log.e(TAG, "URL Connection failed");
            e.printStackTrace();

        }
        return mw;
    }

    private myWeather parseJSON2(JSONObject json) throws JSONException {
        myWeather mw = new myWeather();

        /*
        "main": {
            20         "temp": 306.13,
            21         "pressure": 1006,
            22         "humidity": 67,
            23         "temp_min": 304.15,
            24         "temp_max": 308.15
         */
        mw.setTemp(json.getJSONObject("main").getDouble("temp"));
        mw.setPressure(json.getJSONObject("main").getInt("pressure"));
        mw.setHumidity(json.getJSONObject("main").getInt("humidity"));
        mw.setTemp_min(json.getJSONObject("main").getDouble("temp_min"));
        mw.setTemp_max(json.getJSONObject("main").getDouble("temp_max"));

        /*
        "wind": {
            27         "speed": 3.6,
            28         "deg": 260
        "clouds": {
            31         "all": 20
            32     },

        "name": "Seoul",
        */
        mw.setWind_speed(json.getJSONObject("wind").getDouble("speed"));
        mw.setClouds(json.getJSONObject("clouds").getInt("all"));
        mw.setName(json.getString("name"));
        /*
        "coord": {
            3         "lon": 126.98,
            4         "lat": 37.57
            5     },
            6     "sys": {
            7         "message": 0.3162,
            8         "country": "KR",
            9         "sunrise": 1404245699,
            10         "sunset": 1404298641
            11     },
        */
        mw.setLat(json.getJSONObject("coord").getDouble("lat"));
        mw.setLon(json.getJSONObject("coord").getDouble("lon"));
        mw.setCountry(json.getJSONObject("sys").getString("country"));
        long sunrise = json.getJSONObject("sys").getLong("sunrise");
        long sunset = json.getJSONObject("sys").getLong("sunset");
        mw.setSunrize(new Date(sunrise*100));
        mw.setSunset(new Date(sunset*100));
        return mw;
    }



    public Weather getWeather(double lat,double lon){

        String lats = String.format("%.6f", lat);
        String lons = String.format("%.6f", lon);

        Weather w = new Weather();
        String urlString = openWeatherURL + "?lat="+lats+"&lon="+lons +"&APPID=967b32afdae442657e1041e342e75fb5";
        Log.e("openWeatherURL",urlString);

        try {
            // call API by using HTTPURLConnection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));
            // parse JSON
            w = parseJSON(json);
            w.setIon(lon);
            w.setLat(lat);
        }catch(MalformedURLException e){
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;
        }catch(JSONException e) {
            System.err.println("JSON parsing error");
            e.printStackTrace();
            return null;
        }catch(IOException e){
            System.err.println("URL Connection failed");
            e.printStackTrace();
            return null;
        }
        return w;
    }

    private Weather parseJSON(JSONObject json) throws JSONException {
        Weather w = new Weather();
        w.setTemprature(json.getJSONObject("main").getInt("temp"));
        w.setCity(json.getString("name"));
        return w;
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
