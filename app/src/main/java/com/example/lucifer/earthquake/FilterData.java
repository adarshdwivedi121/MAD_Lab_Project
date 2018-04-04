package com.example.lucifer.earthquake;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.io.IOException;
import java.util.List;

public class FilterData extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView list;
    private LocationListAdapter adapter;
    private PopupWindow window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_data);

        ((EditText)findViewById(R.id.start_dt_data)).setText(Constants.start_time);
        ((EditText)findViewById(R.id.end_dt_data)).setText(Constants.end_time);
        ((EditText)findViewById(R.id.max_mag_data)).setText(Constants.max_mag);
        ((EditText)findViewById(R.id.min_mag_data)).setText(Constants.min_mag);

        findViewById(R.id.add_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window = new PopupWindow(FilterData.this);
                View rootView = getLayoutInflater().inflate(R.layout.fragment_add_location, null);
                rootView.setBackgroundColor(Color.argb(255, 255, 255, 255));
                window.setFocusable(true);

                final SQLiteDatabase db = openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);

                final EditText location = rootView.findViewById(R.id.location_input);
                Button done = rootView.findViewById(R.id.done_button);

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(location.getText().toString().isEmpty())
                                location.setError("Enter a location");
                            else {
                                Geocoder gc = new Geocoder(FilterData.this);
                                if (gc.isPresent()) {
                                    List<Address> list = gc.getFromLocationName(location.getText().toString(), 1);
                                    Address address = list.get(0);
                                    double lat = address.getLatitude();
                                    double lng = address.getLongitude();
                                    StringBuilder builder = new StringBuilder("");
                                    builder.append("insert into ").append(Constants.TABLE_NAME).
                                            append("(").append(Constants.COL_NAME).append(",").append(Constants.COL_LAT).append(",").append(Constants.COL_LONG).append(") values ('").
                                            append(location.getText().toString()).append("',").append(lat).append(",").append(lng).append(");");
                                    Log.i("Geocoder", builder.toString());
                                    db.execSQL(builder.toString());
                                    window.dismiss();
                                    getSupportLoaderManager().initLoader(1, null, FilterData.this).forceLoad();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                window.setContentView(rootView);
                window.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

            }
        });

        list = findViewById(R.id.location_list);
        adapter = new LocationListAdapter(this, null);
        adapter .registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                list.setAdapter(adapter);
                list.invalidate();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                getSupportLoaderManager().initLoader(1, null, FilterData.this).forceLoad();
            }
        });
        getSupportLoaderManager().initLoader(1, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_act_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.clear:
                ((EditText)findViewById(R.id.start_dt_data)).setText(Constants.start_time);
                ((EditText)findViewById(R.id.end_dt_data)).setText(Constants.end_time);
                ((EditText)findViewById(R.id.max_mag_data)).setText(Constants.max_mag);
                ((EditText)findViewById(R.id.min_mag_data)).setText(Constants.min_mag);
                break;
            case R.id.done:
                Constants.start_time = ((EditText)findViewById(R.id.start_dt_data)).getText().toString();
                Constants.end_time = ((EditText)findViewById(R.id.end_dt_data)).getText().toString();
                Constants.max_mag = ((EditText)findViewById(R.id.max_mag_data)).getText().toString();
                Constants.min_mag = ((EditText)findViewById(R.id.min_mag_data)).getText().toString();
                finish();

            default:
                finish();
                break;
        }
        return true;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new LocationListTask(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
