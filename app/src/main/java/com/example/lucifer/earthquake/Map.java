package com.example.lucifer.earthquake;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getSupportActionBar().setTitle(getIntent().getStringExtra("Place"));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng loc = new LatLng(getIntent().getDoubleExtra("Lat", 0.0), getIntent().getDoubleExtra("Long", 0.0));
        float mag = getIntent().getFloatExtra("Mag", 0.0f);
        googleMap.addMarker(new MarkerOptions().position(loc).title(getIntent().getStringExtra("Place")));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(loc, 5);
        googleMap.animateCamera(yourLocation);
        googleMap.addCircle(new CircleOptions()
                .center(loc)
                .radius(50000 * mag)
                .strokeColor(Color.RED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quake_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "An Earthquake occurred in " + getIntent().getStringExtra("place") + ". Check it out..\n" + getIntent().getStringExtra("url"));
                startActivity(Intent.createChooser(share, "Share Using"));
                break;

            default:
                finish();
                break;
        }
        return true;
    }
}
