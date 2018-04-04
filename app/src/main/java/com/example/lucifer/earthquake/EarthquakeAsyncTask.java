package com.example.lucifer.earthquake;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

/**
 * Created by adarsh on 07/05/2017.
 */

public class EarthquakeAsyncTask extends AsyncTaskLoader <ArrayList<Earthquake>>{

    private final Activity activity;
    private boolean sorted = false;
    private String url;

    public EarthquakeAsyncTask(Activity context, String url, boolean sorted) {
        super(context);
        this.activity = context;
        this.url = url;
        this.sorted = sorted;
    }

    @Override
    public ArrayList<Earthquake> loadInBackground() {
        ArrayList<Earthquake> data = DataQuery.getData(this.url);
        if(sorted) {
            ArrayList<Earthquake> res = new ArrayList<>();
            SQLiteDatabase db = activity.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME, null);
            ArrayList<Locations> locations = new ArrayList<>();
            while (cursor.moveToNext()){
                locations.add(new Locations(
                                cursor.getString(cursor.getColumnIndex(Constants.COL_NAME)),
                                cursor.getLong(cursor.getColumnIndex(Constants.COL_LAT)),
                                cursor.getLong(cursor.getColumnIndex(Constants.COL_LONG))
                        )
                );
            }
            cursor.close();

            for (Earthquake quake : data){
                for (Locations loc : locations){
                    if(Constants.getDistance(loc.getLatitude(), loc.getLongitude(), quake.getLatitude(), quake.getLongitude()) <= 500000){
                        res.add(quake);
                        break;
                    }
                }
            }

            return res;
        }
        else
            return data;
    }
}
