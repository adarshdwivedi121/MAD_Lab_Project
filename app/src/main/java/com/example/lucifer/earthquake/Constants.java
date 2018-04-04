package com.example.lucifer.earthquake;

import android.database.Cursor;

public class Constants {
    public static String DATABASE_NAME = "Temblor";

    public static String TABLE_NAME = "locations";
    public static String COL_ID = "_id";
    public static String COL_NAME = "place";
    public static String COL_LAT = "latitude";
    public static String COL_LONG = "longitude";

    public static String URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
    public static String start_time = "2014-01-01"; //&starttime=2000-01-01
    public static String end_time = "2014-01-02"; //&endtime=2016-05-02
    public static String min_mag = "1"; //&minmagnitude=5
    public static String max_mag = "20"; //&maxmagnitude=5

    public static volatile boolean serviceStatus = false;
    public static volatile boolean dummyAlert = false;
    public static volatile boolean dummyTsunami = false;

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
