package com.example.lucifer.earthquake;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class LocationListAdapter extends CursorAdapter {
    private Activity activity;

    public LocationListAdapter(Activity activity, Cursor c) {
        super(activity, c, 0);
        this.activity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.location_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if(view == null)
            view = LayoutInflater.from(context).inflate(R.layout.location_item, null);

        final TextView place = view.findViewById(R.id.location_place);
        TextView latitude = view.findViewById(R.id.location_latitude);
        TextView longitude = view.findViewById(R.id.location_longitude);

        place.setText(cursor.getString(cursor.getColumnIndex(Constants.COL_NAME)));
        place.setTag(cursor.getInt(cursor.getColumnIndex(Constants.COL_ID)));
        latitude.setText(cursor.getString(cursor.getColumnIndex(Constants.COL_LAT)));
        longitude.setText(cursor.getString(cursor.getColumnIndex(Constants.COL_LONG)));

        view.findViewById(R.id.delete_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder query = new StringBuilder("");
                query.append("delete from ").append(Constants.TABLE_NAME).append(" where ").append(Constants.COL_ID).append("=").append(place.getTag()).append(";");
                activity.openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null).execSQL(query.toString());
                notifyDataSetInvalidated();
            }
        });
    }
}
