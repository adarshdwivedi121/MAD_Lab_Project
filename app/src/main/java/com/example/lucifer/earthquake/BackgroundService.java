package com.example.lucifer.earthquake;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static com.example.lucifer.earthquake.Constants.end_time;
import static com.example.lucifer.earthquake.Constants.getDistance;
import static com.example.lucifer.earthquake.Constants.max_mag;
import static com.example.lucifer.earthquake.Constants.min_mag;
import static com.example.lucifer.earthquake.Constants.start_time;

public class BackgroundService extends Service implements LocationListener{
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private Context context;

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    LocationManager locationManager;
    Location currentLocation;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 10000;
    public static String str_receiver = "earthquake.service.receiver";
    Intent intent;
    private SQLiteDatabase db;
    private NotificationManager mNotificationManager;

    @SuppressLint("MissingPermission")
    private void fn_getlocation(){
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isNetworkEnable || isGPSEnable) {
            if (isNetworkEnable){
                currentLocation = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (currentLocation!=null){
                        fn_update(currentLocation);
                    }
                }
            }

            if (isGPSEnable){
                currentLocation= null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (currentLocation!=null){
                        fn_update(currentLocation);
                    }
                }
            }
        }
    }

    private void fn_update(Location location){
        intent.putExtra("latutide",location.getLatitude()+"");
        intent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(intent);
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            if(Constants.serviceStatus)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("EarthquakeService", THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Log.i("Earthquake Background", "Starting Service");
        db = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(),5,notify_interval);
        intent = new Intent(str_receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            for(;;) {
                if (Constants.serviceStatus) {
                    Log.i("Earthquake Background", "RUNNING QUERY");
                    String req = Constants.URL + "&starttime=" + start_time + "&endtime=" + end_time + "&minmagnitude=" + min_mag + "&maxmagnitude=" + max_mag;
                    ArrayList<Earthquake> earthquakes = DataQuery.getData(req);
                    Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME, null);
                    ArrayList<Locations> locations = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        locations.add(new Locations(
                                        cursor.getString(cursor.getColumnIndex(Constants.COL_NAME)),
                                        cursor.getLong(cursor.getColumnIndex(Constants.COL_LAT)),
                                        cursor.getLong(cursor.getColumnIndex(Constants.COL_LONG))
                                )
                        );
                    }
                    cursor.close();
                    Log.i("Location", currentLocation.getLatitude() + "\t" + currentLocation.getLongitude());

                    for (Earthquake earthquake : earthquakes) {
                        if (getDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), earthquake.getLatitude(), earthquake.getLongitude()) < 100000 &&
                                Double.parseDouble(earthquake.getMag()) > 5.0) {
                            alertUser();
                        }
                    }
                    if (Constants.dummyAlert)
                        alertUser();

                    for (Locations location : locations) {
                        for (Earthquake earthquake : earthquakes) {
                            double dist = Constants.getDistance(location.getLatitude(), location.getLongitude(), earthquake.getLatitude(), earthquake.getLongitude());
                            if (dist <= 500000) {
                                int notifyID = 1;
                                boolean vis = false;
                                for (StatusBarNotification notification : mNotificationManager.getActiveNotifications()) {
                                    if (notification.getId() == notifyID) {
                                        vis = true;
                                    }
                                }
                                if (!vis) {
                                    String CHANNEL_ID = "my_channel_01";// The id of the channel.
                                    CharSequence name = "Earthquake";// The user-visible name of the channel.
                                    int importance = NotificationManager.IMPORTANCE_HIGH;
                                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
// Create a notification and set the notification channel.
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                    String content = "An Earthquake occurred near " + location.getName() + ".";
                                    if (earthquake.isTsunami())
                                        content += "There is a Tsunami as well.";
                                    Notification notification = new Notification.Builder(getApplicationContext())
                                            .setContentTitle("Earthquake Alert")
                                            .setContentText(content)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setChannelId(CHANNEL_ID)
                                            .setContentIntent(pIntent)
                                            .build();

                                    mNotificationManager.createNotificationChannel(mChannel);

// Issue the notification.
                                    mNotificationManager.notify(notifyID, notification);
                                }
                                Log.e("Earthquake Background", "earthquake occured");
                                break;
                            }
                        }
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void alertUser() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
        r.play();

        int notifId = 2;
        boolean vis = false;
        for (StatusBarNotification notification : mNotificationManager.getActiveNotifications()) {
            if (notification.getId() == notifId) {
                vis = true;
            }
        }
        if(!vis) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "Earthquake";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
// Create a notification and set the notification channel.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            String content = "Earthquake Nearby. Get in Open.";
            if(Constants.dummyTsunami)
                content = "There is a Tsunami. Reach higher Ground";
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Earthquake Alert")
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pIntent)
                    .build();

            mNotificationManager.createNotificationChannel(mChannel);

// Issue the notification.
            mNotificationManager.notify(notifId, notification);
        }
    }
}
