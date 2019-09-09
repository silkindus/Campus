package com.example.campus;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class OrderAdapter extends ArrayAdapter<Item> {

    //adapter for manipulation with ListView

    List<Item> itemData;
    int resource;

    public OrderAdapter(Context context, int resource, List<Item> objects) {
        super(context, resource, objects);

        itemData = objects;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        final Item currentItem = itemData.get(position);

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_order_adapter, parent, false);
        }

        TextView itemName = (TextView) listItemView.findViewById(R.id.item_name);
        itemName.setText(currentItem.getName());

        Spinner itemQuantity = (Spinner) listItemView.findViewById(R.id.quantity_spinner);

        String arr[] = new String[50];

        for(int i = 1; i < 51; i++){
            arr[i - 1] = String.valueOf(i);
        }

        TextView itemPrice = (TextView) listItemView.findViewById(R.id.total_price);
        itemPrice.setText(String.format(Locale.US, "%.2f", Float.parseFloat(currentItem.getPrice())).concat("€"));

        if (resource == 0) {
            //from Purchase activity

            final int[] prevValue = {Integer.parseInt(Purchase.orderItemList.get(position).getQuantity())};

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, arr);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            itemQuantity.setAdapter(spinnerArrayAdapter);
            itemQuantity.setSelection(prevValue[0] - 1);

            itemQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    Purchase.orderItemList.get(position).setQuantity(String.valueOf(pos + 1));
                    Purchase.totalPrice += (pos + 1 - prevValue[0]) * Float.parseFloat(currentItem.getPrice());
                    Purchase.totalPriceText.setText(String.valueOf(Purchase.totalPrice));
                    prevValue[0] = pos + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            final ImageView menu = (ImageView) listItemView.findViewById(R.id.menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(getContext(), menu);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.item_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().matches("Remove")) {
                                Item shopItem = Purchase.orderItemList.get(position);
                                Purchase.orderItemList.remove(position);
                                Purchase.totalPrice -= Float.parseFloat(shopItem.getPrice()) * Integer.parseInt(shopItem.getQuantity());
                                Purchase.totalPriceText.setText(String.format("%.2f", Purchase.totalPrice));
                                Purchase.purchase.makeListView();
                            }
                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        if (resource == 1){
            //from ShowReceipts activity

            final int[] prevValue = {Integer.parseInt(ShowReceipts.orderItemList.get(position).getQuantity())};

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, arr);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            itemQuantity.setAdapter(spinnerArrayAdapter);
            itemQuantity.setSelection(prevValue[0] - 1);

            itemQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    ShowReceipts.orderItemList.get(position).setQuantity(String.valueOf(pos + 1));
                    ShowReceipts.totalPrice += (pos + 1 - prevValue[0]) * Float.parseFloat(currentItem.getPrice());
                    ShowReceipts.totalPriceText.setText(String.valueOf(ShowReceipts.totalPrice));
                    prevValue[0] = pos + 1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            final ImageView menu = (ImageView) listItemView.findViewById(R.id.menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(getContext(), menu);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.item_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().matches("Remove")) {
                                Item shopItem = ShowReceipts.orderItemList.get(position);
                                ShowReceipts.orderItemList.remove(position);
                                ShowReceipts.totalPrice -= Float.parseFloat(shopItem.getPrice()) * Integer.parseInt(shopItem.getQuantity());
                                ShowReceipts.totalPriceText.setText(String.format("%.2f", ShowReceipts.totalPrice));
                                ShowReceipts.showReceipts.makeListView();
                            }
                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        return listItemView;
    }
}

