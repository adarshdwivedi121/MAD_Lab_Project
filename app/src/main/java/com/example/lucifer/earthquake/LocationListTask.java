package com.example.lucifer.earthquake;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

class LocationListTask extends AsyncTaskLoader<Cursor> {
    private Activity activity;

    public LocationListTask(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Nullable
    @Override
    public Cursor loadInBackground() {
        return activity.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE,null).rawQuery("select * from " + Constants.TABLE_NAME + ";", null);
    }
}
