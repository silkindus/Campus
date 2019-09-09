package com.example.campus;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ShowStats extends Activity {

    //shows in table stats of sold items by days

    private SimpleDateFormat dayFormat = new SimpleDateFormat("d");
    private HashMap<String, List<Item>> mapDays = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stats);

        dayFormat.setTimeZone(TimeZone.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());

        loadData();

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.WHITE);
        TableLayout tableLayout = createTableLayout(mapDays);
        HorizontalScrollView hsv = new HorizontalScrollView(this);

        hsv.addView(tableLayout);
        sv.addView(hsv);
        setContentView(sv);
    }

    private void loadData() {
        List<String> receiptsList = new ArrayList<>();

        final File receiptsFile = new File(getFilesDir(), "receipts");

        if (receiptsFile != null) {
            BufferedReader reader = null;
            String line;
            try {
                reader = new BufferedReader(new FileReader(receiptsFile));
                while ((line = reader.readLine()) != null) {
                    receiptsList.add(line);
                }
                reader.close();
            } catch (Exception e) {
                Log.e("error", "Unable to read file.");
            }
        }

        for (String receiptID : receiptsList) {
            File receiptFile = new File(getFilesDir(), receiptID);

            if (receiptFile != null) {
                BufferedReader reader = null;
                String line;
                try {
                    reader = new BufferedReader(new FileReader(receiptFile));
                    line = reader.readLine();
                    String[] data = line.split(";");

                    String day = dayFormat.format(new Date(Long.parseLong(receiptID)));

                    while ((line = reader.readLine()) != null) {
                        String[] itemData = line.split(";");

                        if (mapDays.get(day) == null) {
                            List<Item> items = new ArrayList<>();
                            items.add(new Item(itemData[0], itemData[3], Long.parseLong(receiptID)));
                            mapDays.put(day, items);
                        } else {
                            boolean occured = false;
                            List<Item> items = mapDays.get(day);
                            for (int i = 0; i < items.size(); i++) {
                                Item item = items.get(i);
                                if (item.getName().matches(itemData[0])) {
                                    items.get(i).setQuantity(String.valueOf(Integer.parseInt(item.getQuantity()) + Integer.parseInt(itemData[3])));
                                    occured = true;
                                }
                            }
                            if (!occured) {
                                items.add(new Item(itemData[0], itemData[3], Long.parseLong(receiptID)));
                            }
                            mapDays.put(day, items);
                        }
                    }
                    reader.close();

                } catch (Exception e) {

                }
            }
        }
    }

    private TableLayout createTableLayout(HashMap<String, List<Item>> mapDays) {
        HashMap<String, Integer> itemsColumn = new HashMap<>();

        for (Map.Entry<String, List<Item>> entry : mapDays.entrySet()) {
            for (Item item : entry.getValue()) {
                if (!itemsColumn.containsKey(item.getName())) {
                    itemsColumn.put(item.getName(), itemsColumn.size());
                }
            }
        }

        // 1) Create a tableLayout and its params
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setBackgroundColor(Color.BLACK);

        // 2) create tableRow params
        TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams();
        tableRowParams.setMargins(1, 1, 1, 1);
        tableRowParams.weight = 1;

        TableRow tableRow = new TableRow(this);
        tableRow.setBackgroundColor(Color.BLACK);

        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Items/Days");
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextSize(35);
        textView.setPadding(4, 4, 4, 4);

        tableRow.addView(textView, tableRowParams);

        for (String itemName : itemsColumn.keySet()) {
            textView = new TextView(this);
            textView.setBackgroundColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setText(itemName);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(35);
            textView.setPadding(4, 4, 4, 4);

            tableRow.addView(textView, tableRowParams);
        }

        tableLayout.addView(tableRow, tableLayoutParams);

        int mapSize = mapDays.size();
        for (int i = 0; i < mapSize; i++) {

            List<TextView> textViews = new ArrayList<>();
            long lowestTime = (long) Math.pow(10, 14);
            Map.Entry<String, List<Item>> lowestEntry = null;

            tableRow = new TableRow(this);
            tableRow.setBackgroundColor(Color.BLACK);

            for (Map.Entry<String, List<Item>> entry : mapDays.entrySet()) {
                if (lowestTime > entry.getValue().get(0).getTime()) {
                    lowestTime = entry.getValue().get(0).getTime();
                    lowestEntry = entry;
                }
            }

            textView = new TextView(this);
            textView.setBackgroundColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setText(dateFormat.format(new Date(lowestEntry.getValue().get(0).getTime())));
            tableRow.addView(textView, tableRowParams);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(20);

            for (String itemName : itemsColumn.keySet()) {
                textView = new TextView(this);
                textView.setBackgroundColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                textView.setText("0");
                textView.setTextSize(20);
                textViews.add(textView);
                tableRow.addView(textView, tableRowParams);
            }

            for (Item item : lowestEntry.getValue()) {
                textViews.get(itemsColumn.get(item.getName())).setText(item.getQuantity());
            }

            mapDays.entrySet().remove(lowestEntry);
            tableLayout.addView(tableRow, tableLayoutParams);
        }

        return tableLayout;
    }
}
