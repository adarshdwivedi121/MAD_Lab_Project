package com.example.lucifer.earthquake;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by adarsh on 9/3/17.
 */

public class EarthquakesAdapter extends ArrayAdapter<Earthquake> {
    private int[] color = {
            R.color.mag1,
            R.color.mag2,
            R.color.mag3,
            R.color.mag4,
            R.color.mag5,
            R.color.mag6,
            R.color.mag7,
            R.color.mag8,
            R.color.mag9,
            R.color.mag10plus
    };
    public EarthquakesAdapter(Context context, ArrayList<Earthquake> resource) {
        super(context, 0, resource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        final Earthquake curr_quake = getItem(position);
        TextView magView = (TextView) convertView.findViewById(R.id.magnitude);
        magView.setText(curr_quake.getMag());
        GradientDrawable circle = (GradientDrawable) magView.getBackground();
        circle.setColor(getMagColor(curr_quake.getMagVal()));

        TextView dirView = (TextView) convertView.findViewById(R.id.distance);
        dirView.setText(curr_quake.getDir());
        TextView locationView = (TextView) convertView.findViewById(R.id.location);
        locationView.setText(curr_quake.getPlace());

        TextView dateView = (TextView) convertView.findViewById(R.id.date);
        dateView.setText(curr_quake.getDate());
        TextView timeView = (TextView) convertView.findViewById(R.id.time);
        timeView.setText(curr_quake.getTime());

        return convertView;
    }

    private int getMagColor(float mag) {
        int m = (int) Math.floor(mag);
        String s = "R.color.mag";
        if (m<=9) return ContextCompat.getColor(getContext(), color[m-1]);
        return ContextCompat.getColor(getContext(), color[9]);
    }
}
