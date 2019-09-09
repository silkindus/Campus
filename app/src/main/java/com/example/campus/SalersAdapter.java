package com.example.campus;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SalersAdapter extends ArrayAdapter<String> {

    //ListView adapter, shoes images of salers

    List<String> salers;
    List<Integer> imageID;
    int resource;

    public SalersAdapter(Context context, int resource, List<String> objects, List<Integer> imageID) {
        super(context, resource, objects);

        salers = objects;
        this.imageID = imageID;
        this.resource = resource;
    }

    public SalersAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);

        salers = objects;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_salers_adapter, parent, false);
        }

        if (resource == 0) {
            ImageView salerImage = (ImageView) listItemView.findViewById(R.id.saler_image);
            salerImage.setVisibility(View.VISIBLE);
            salerImage.setImageResource(imageID.get(position));
        }

        if (resource == 1){
            TextView selerName = (TextView) listItemView.findViewById(R.id.saler_name);
            selerName.setVisibility(View.VISIBLE);
            selerName.setText(salers.get(position));
        }

        return listItemView;
    }
}

