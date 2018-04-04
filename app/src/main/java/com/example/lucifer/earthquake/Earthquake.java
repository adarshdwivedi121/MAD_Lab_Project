package com.example.lucifer.earthquake;

import java.text.DecimalFormat;

public class Earthquake {
    private float mag;
    private String dir;
    private String place;

    private double latitude;
    private double longitude;

    private boolean tsunami = false;

    private String date;
    private String time;
    private String url;
    private DecimalFormat f = new DecimalFormat("0.0");

    Earthquake(){
        mag = 0;
        latitude = longitude = 0.0f;
        dir = place = url = "";
        date = "1 Jan, 1970";
        time = "00:00 AM";
    }

    Earthquake(float m, String p, String d, double lat, double lon, String u, int tsunami){
        mag = m;
        dir = "";
        place = p;
        this.latitude = lat;
        this.longitude = lon;
        if(tsunami == 1) this.tsunami = true;
        date = d;
        time = "00:00 AM";
        url = u;
    }
    Earthquake(float m, String d, String p, double lat, double lon, String da, String t, String u, int tsunami){
        mag = m;
        dir = d;
        place = p;
        this.latitude = lat;
        this.longitude = lon;
        if(tsunami == 1) this.tsunami = true;
        date = da;
        time = t;
        url = u;
    }

    public float getMagVal(){ return mag; }
    public String getMag(){ return f.format(mag); }
    public String getDir(){
        return dir;
    }
    public String getPlace(){
        return place;
    }
    public String getDate(){
        return date;
    }
    public String getTime(){
        return time;
    }
    public String getUrl() { return url; }
    public double getLatitude() { return latitude;  }
    public double getLongitude() { return longitude;  }

    public boolean isTsunami() { return tsunami; }
}
