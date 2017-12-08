package com.joonho.runme.util;

/**
 * Created by user on 2017-12-08.
 */
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

public class WeatherAPI {

    public class Weather {
        int lat;
        int ion;
        int temprature;
        int cloudy;
        String city;

        public void setLat(int lat){ this.lat = lat;}
        public void setIon(int ion){ this.ion = ion;}
        public void setTemprature(int t){ this.temprature = t;}
        public void setCloudy(int cloudy){ this.cloudy = cloudy;}
        public void setCity(String city){ this.city = city;}

        public int getLat(){ return lat;}
        public int getIon() { return ion;}
        public int getTemprature() { return temprature;}
        public int getCloudy() { return cloudy; }
        public String getCity() { return city; }
    }


    final static String openWeatherURL = "http://api.openweathermap.org/data/2.5/weather";
    public Weather getWeather(int lat,int lon){
        Weather w = new Weather();
        String urlString = openWeatherURL + "?lat="+lat+"&lon="+lon;

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
