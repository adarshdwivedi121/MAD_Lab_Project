package com.example.lucifer.earthquake;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.lucifer.earthquake.Constants.URL;
import static com.example.lucifer.earthquake.Constants.end_time;
import static com.example.lucifer.earthquake.Constants.max_mag;
import static com.example.lucifer.earthquake.Constants.min_mag;
import static com.example.lucifer.earthquake.Constants.start_time;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Earthquake>>{

    static private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    static private ArrayList<Earthquake> earthquakes = null;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Earthquakes");

        db = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        db.execSQL("create table if not exists " + Constants.TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, place VARCHAR, latitude VARCHAR, longitude VARCHAR);");

        Calendar cal = Calendar.getInstance();
        end_time= format.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        start_time = format.format(cal.getTime());

        if(checkConnect()) {
            if (earthquakes == null)
                refresh();
            else
                displayData();
        }

    }

    private boolean checkConnect(){
        NetworkInfo activeNetwork = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
            return true;
        else{
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            findViewById(R.id.activity_main).setVisibility(View.GONE);
            findViewById(R.id.error_text).setVisibility(View.VISIBLE);
            findViewById(R.id.error_image).setVisibility(View.VISIBLE);

            ((TextView)findViewById(R.id.error_text)).setText(R.string.network_error);
            return false;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(checkConnect()) {
            if (earthquakes == null)
                refresh();
            else
                displayData();
        }
    }

    void displayData(){
        Log.e("Display", "Array Length" + Integer.toString(earthquakes.size()));
        findViewById(R.id.activity_main).setVisibility(View.VISIBLE);
        final EarthquakesAdapter adapter = new EarthquakesAdapter(getApplicationContext(), earthquakes);
        ListView numList= findViewById(R.id.activity_main);
        numList.setAdapter(adapter);
        numList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Earthquake quake =  adapter.getItem(i);
                Intent ev = new Intent(getApplicationContext(), Map.class);
                ev.putExtra("Lat", quake.getLatitude());
                ev.putExtra("Long", quake.getLongitude());
                ev.putExtra("Place", quake.getPlace());
                ev.putExtra("Mag", quake.getMagVal());
                ev.putExtra("url", quake.getUrl());
                startActivity(ev);
            }
        });
    }

    private void refresh(){
        if(checkConnect())
            getSupportLoaderManager().initLoader(1, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_act_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getSupportLoaderManager().destroyLoader(1);
        switch(item.getItemId()){
            case R.id.filter:
                earthquakes = null;
                startActivity(new Intent(getApplicationContext(), FilterData.class));
                break;

            case R.id.refresh:
                refresh();
                break;

            case R.id.dummy_earthquake:
                Constants.dummyAlert = !Constants.dummyAlert;
                break;

            case R.id.dummy_tsunami:
                Constants.dummyTsunami = !Constants.dummyTsunami;
                break;

            case R.id.show_added:
                getSupportLoaderManager().initLoader(1, new Bundle(), this).forceLoad();
                break;

            case R.id.toggle_service:
                Constants.serviceStatus = !Constants.serviceStatus;
                if(Constants.serviceStatus) {
                    Toast.makeText(this, "Background Tracking Enabled", Toast.LENGTH_LONG).show();
                    startService(new Intent(this, BackgroundService.class));
                }
                else {
                    Toast.makeText(this, "Background Tracking Disabled", Toast.LENGTH_LONG).show();
                    stopService(new Intent(this, BackgroundService.class));
                }

        }
        return true;
    }

    @Override
    public Loader<ArrayList<Earthquake>> onCreateLoader(int id, Bundle args) {
        findViewById(R.id.error_text).setVisibility(View.GONE);
        findViewById(R.id.error_image).setVisibility(View.GONE);
        findViewById(R.id.activity_main).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        String req = URL + "&starttime=" + start_time + "&endtime=" + end_time + "&minmagnitude=" + min_mag  + "&maxmagnitude=" + max_mag;
        Log.e("REQ URL", req);
        Log.e("Loader", "Loader Created");

        if(args == null)
            return new EarthquakeAsyncTask(MainActivity.this, req, false);

        else
            return new EarthquakeAsyncTask(MainActivity.this, req, true);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Earthquake>> loader, ArrayList<Earthquake> earthquakes) {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        if(earthquakes != null) {
            MainActivity.earthquakes = earthquakes;
            displayData();
            Log.e("ARRAY len", Integer.toString(earthquakes.size()));

            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Earthquake Occured Nearby")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // hide the notification after its selected
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, notification);
        }
        else {
            ((TextView)findViewById(R.id.error_text)).setText(R.string.no_earthquake);
            findViewById(R.id.error_text).setVisibility(View.VISIBLE);
        }
        getSupportLoaderManager().destroyLoader(1);
        Log.e("Loader", "Loader Destroyed");
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Earthquake>> loader) {
    }
}
