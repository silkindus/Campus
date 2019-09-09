package com.example.campus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GridViewAdapter extends BaseAdapter {

    //defines the elements that display in GridView

    private List<Item> itemList;
    private static LayoutInflater inflater = null;
    private Context context;

    public GridViewAdapter(Context context, List<Item> itemData){
        this.itemList = itemData;
        this.context = context;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        rowView = inflater.inflate(R.layout.activity_grid_view_adapter, null);
        TextView name = rowView.findViewById(R.id.item_name);
        TextView price = rowView.findViewById(R.id.price);
        ImageView image = rowView.findViewById(R.id.image);
        TextView discount = rowView.findViewById(R.id.discount);

        //filling of TextViews with data

        name.setText(itemList.get(position).getName());
        price.setText(String.format(Locale.US, "%s€", String.format(Locale.US, "%.2f", Float.parseFloat(itemList.get(position).getPrice().replaceAll(",", ".")))));

        if (itemList.get(position).getDiscount() > 0){
            discount.setVisibility(View.VISIBLE);
            discount.setText(String.format(Locale.US, "%.1f", itemList.get(position).getDiscount()).concat(" %"));
        }else discount.setVisibility(View.INVISIBLE);

        //loads image from local directory

        File directory = context.getDir("Images", Context.MODE_PRIVATE);
        File imageFile = new File(directory, itemList.get(position).getImage() + ".png");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imageFile);
            image.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rowView;
    }

}
