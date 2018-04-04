package com.example.lucifer.earthquake;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lucifer on 14/3/18.
 */

public class DataQuery {
    public static ArrayList<Earthquake> getData(String reqURL) {
        String response = null;
        try {
            //request http handler to get data
            response = makeHttpRequest(new URL(reqURL));
        } catch (Exception e) {
            Log.e("Request", "Problem Sending Request to the Server\n" + e.getMessage());
        }

        //return list of Weather Data
        return extractAttributes(response);
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    private static ArrayList<Earthquake> extractAttributes(String response){
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        ArrayList<Earthquake> earthquakes = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(response);
            JSONArray data = root.getJSONArray("features");
            for(int i = 0; i<data.length(); i++){
                JSONObject o = data.getJSONObject(i).getJSONObject("properties");
                float mag = (float)o.getDouble("mag");
                String place = o.getString("place");
                String url = o.getString("url");
                int ts = o.getInt("tsunami");
                double lon = data.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
                double lat = data.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
                SimpleDateFormat f = new SimpleDateFormat("dd MMM, yyyy - hh:mm aaa");
                String date = f.format(new Date(o.getLong("time")));
                String[] dt = date.split(" - ");
                if(place.contains(", ")) {
                    String pl[] = place.split(", ");
                    earthquakes.add(new Earthquake(mag, pl[0], pl[1], lat, lon, dt[0], dt[1], url, ts));
                }
                else
                    earthquakes.add(new Earthquake(mag, "near the", place, lat, lon, dt[0], dt[1], url, ts));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String response = "";

        // If the URL is null, then return early.
        if (url == null) {
            return response;
        }

        HttpURLConnection urlConn = null;   //URL connection handler
        InputStream inpStream = null;       //input stream Handler
        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setReadTimeout(100000);
            urlConn.setConnectTimeout(150000);
            urlConn.setRequestMethod("GET");
            urlConn.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConn.getResponseCode() == 200) {
                inpStream = urlConn.getInputStream();
                response = readFromStream(inpStream);
            } else {
                Log.e("HTTP Handler", "Error response code: " + urlConn.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("HTTP Handler", "Problem retrieving the Weather Data JSON results.", e);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
            if (inpStream != null) {
                inpStream.close();
            }
        }
        return response;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}