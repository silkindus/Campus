package com.example.campus;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Purchase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Item> itemList = new ArrayList<>();
    public static List<Item> orderItemList = new ArrayList<>();
    private GridView gridView;
    private ListView listView;
    private ConstraintLayout questionConstraint;
    private Button yes;
    private Button no;
    private Button money;
    private Button card;
    private int gridPosition;
    public static TextView totalPriceText;
    public static float totalPrice;
    private Button cancel;
    private TextView search;
    private EditText searchEdit;
    private Button find;
    private TextView sure;
    public static Purchase purchase;
    private boolean payWithMoney = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.MM.yyyy - HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.yyyy");
    private SimpleDateFormat dayFormat = new SimpleDateFormat("d");
    private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
    private RelativeLayout itemsLayout;
    private List<Button> itemButtons = new ArrayList<>();
    public static int BUTTON_WIDTH = 150;
    public static int BUTTON_HEIGTH = 150;
    private boolean fromCancel = false;

    @Override
    protected void onResume() {
        super.onResume();

        itemList.clear();
        loadData();
        makeListView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveItemsToFile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        purchase = this;

        listView = findViewById(R.id.order_list);
        questionConstraint = findViewById(R.id.question_constraint);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        money = findViewById(R.id.money);
        card = findViewById(R.id.card);
        totalPriceText = findViewById(R.id.total_price);
        cancel = findViewById(R.id.cancel);
        search = findViewById(R.id.search);
        searchEdit = findViewById(R.id.search_edit);
        find = findViewById(R.id.find);
        itemsLayout = findViewById(R.id.items_layout);
        sure = findViewById(R.id.sure);

        //sets local timezone for date formats

        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        dayFormat.setTimeZone(TimeZone.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        hourFormat.setTimeZone(TimeZone.getDefault());

        loadData();

        //button listeners

        money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payWithMoney = true;
                fromCancel = false;
                sure.setText("Are you sure - CASH");
                questionConstraint.setVisibility(View.VISIBLE);
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payWithMoney = false;
                fromCancel = false;
                sure.setText("Are you sure - CARD");
                questionConstraint.setVisibility(View.VISIBLE);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromCancel = true;
                sure.setText("Are you sure - CANCEL");
                questionConstraint.setVisibility(View.VISIBLE);
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromCancel) {
                    //clears receipt, removes it from listview
                    questionConstraint.setVisibility(View.GONE);
                    orderItemList.clear();
                    totalPrice = 0;
                    totalPriceText.setText(String.format("%.2f", totalPrice));
                    makeListView();
                } else {
                    //add new receipt to the all other into local file
                    if (orderItemList.size() > 0) {
                        long curTime = System.currentTimeMillis();
                        String receiptData = MainActivity.curSaler + ";" + payWithMoney + ";" + totalPrice + "\n";
                        for (Item item : orderItemList) {
                            receiptData += item.getName() + ";" + item.getPrice() + ";" + item.getImage() + ";" + item.getQuantity() + ";" +
                                    item.getDiscount() + "\n";
                        }

                        FileOutputStream outputStream;

                        try {

                            outputStream = openFileOutput(String.valueOf(curTime), Context.MODE_PRIVATE);
                            outputStream.write(receiptData.getBytes());
                            outputStream.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            outputStream = openFileOutput("receipts", Context.MODE_APPEND);
                            outputStream.write((String.valueOf(curTime) + "\n").getBytes());
                            outputStream.close();

                            questionConstraint.setVisibility(View.GONE);
                            orderItemList.clear();
                            totalPrice = 0;
                            totalPriceText.setText(String.format("%.2f", totalPrice));
                            makeListView();
                            Toast.makeText(getApplicationContext(), "Receipt added", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(getApplicationContext(), "Add some items", Toast.LENGTH_LONG).show();
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionConstraint.setVisibility(View.GONE);
            }
        });

        //initialization of navigation view because of the side menu

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadData() {
        //loads and stores all data needed into data structures
        File itemsFile = new File(getFilesDir(), "items");

        if (itemsFile != null) {
            BufferedReader reader = null;
            String line;
            try {
                reader = new BufferedReader(new FileReader(itemsFile));
                while ((line = reader.readLine()) != null) {
                    String[] itemData = line.split(";");
                    itemList.add(new Item(itemData[0], itemData[1], itemData[2], Float.parseFloat(itemData[3]), Integer.parseInt(itemData[5]),
                            Integer.parseInt(itemData[6])));
                }
                reader.close();
            } catch (Exception e) {
                Log.e("error", "Unable to read file.");
            }

            drawButtons();
        }
    }

    //creates button to every single item that is avaliable

    private void drawButtons() {
        //minimal size of scroll view
        int minSize = 1000;
        if (minSize < (((itemList.size() / 3) + 1) * 200))
            minSize = (((itemList.size() / 3) + 1) * 200);
        itemsLayout.setMinimumHeight(minSize);
        itemsLayout.removeAllViewsInLayout();

        //goes through all items
        for (int i = 0; i < itemList.size(); i++) {
            final Item item = itemList.get(i);

            itemButtons.add(new Button(Purchase.this));
            itemButtons.get(i).setText(item.getName() + "\n" + String.format("%.2f", Float.parseFloat(item.getPrice().replaceAll(",", "."))) + "€");
            itemButtons.get(i).setTextColor(Color.parseColor("#FFFFFF"));
            itemButtons.get(i).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_corner_button));

            File directory = getApplicationContext().getDir("Images", Context.MODE_PRIVATE);
            final File imageFile = new File(directory, item.getImage() + ".png");
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(imageFile);
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

            //sets button params
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    BUTTON_WIDTH,
                    BUTTON_HEIGTH);
            layoutParams.topMargin = item.getY();
            layoutParams.leftMargin = item.getX();
            itemButtons.get(i).setLayoutParams(layoutParams);

            itemsLayout.addView(itemButtons.get(i));

            final int finalI = i;
            itemButtons.get(finalI).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    itemButtons.get(finalI).setVisibility(View.INVISIBLE);
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(itemButtons.get(finalI));

                    ClipData.Item item = new ClipData.Item(String.valueOf(finalI));
                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

                    ClipData data = new ClipData(String.valueOf(finalI), mimeTypes, item);

                    itemButtons.get(finalI).startDrag(data, myShadow, null, 0);

                    return true;
                }
            });

            //after click add item into ordered items
            itemButtons.get(finalI).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Item item = itemList.get(finalI);
                    orderItemList.add(new Item(item.getName(), item.getPrice(), item.getImage(), "1", item.getDiscount()));
                    totalPrice += Float.parseFloat(item.getPrice());
                    totalPriceText.setText(String.format("%.2f", totalPrice));
                    makeListView();
                }
            });

            final float[] finalX = {0};
            final float[] finalY = {0};
            final int[] buttonIndex = new int[1];

            //movement of button
            itemsLayout.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent dragEvent) {
                    if (dragEvent.getClipDescription() != null)
                        buttonIndex[0] = Integer.parseInt(dragEvent.getClipDescription().getLabel().toString());

                    if (dragEvent.getX() != 0 || dragEvent.getY() != 0) {
                        finalX[0] = dragEvent.getX();
                        finalY[0] = dragEvent.getY();
                    }

                    if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                        itemsLayout.removeView((View) itemButtons.get(buttonIndex[0]));

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                BUTTON_WIDTH,
                                BUTTON_HEIGTH);
                        params.topMargin = (int) finalY[0] - BUTTON_HEIGTH / 2;
                        params.leftMargin = (int) finalX[0] - BUTTON_WIDTH / 2;

                        itemList.get(buttonIndex[0]).setX((int) finalX[0] - BUTTON_WIDTH / 2);
                        itemList.get(buttonIndex[0]).setY((int) finalY[0] - BUTTON_HEIGTH / 2);

                        itemButtons.get(buttonIndex[0]).setLayoutParams(params);

                        itemsLayout.addView(itemButtons.get(buttonIndex[0]));
                        itemButtons.get(buttonIndex[0]).setVisibility(View.VISIBLE);
                    }

                    return true;
                }
            });
        }
    }

    public void makeListView() {
        listView.setAdapter(new OrderAdapter(getApplicationContext(), 0, orderItemList));
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        saveItemsToFile();
        int id = menuItem.getItemId();

        if (id == R.id.export_to_excel) {
            exportToExcel();
        } else if (id == R.id.manage_items) {
            Intent intent = new Intent(Purchase.this, ManageItems.class);
            startActivity(intent);
        } else if (id == R.id.show_receipts) {
            Intent intent = new Intent(Purchase.this, ShowReceipts.class);
            startActivity(intent);
        } else if (id == R.id.show_stats){
            Intent intent = new Intent(Purchase.this, ShowStats.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //expots all receipts into excel and stores them into Firebase storage
    //4 types of generated statistics
    //1. all receipts with items
    //2. total items sold
    //3. items sold by days
    //4. items sold by hours

    private void exportToExcel() {
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

        Workbook wb = new HSSFWorkbook();

        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        Cell c;

        int numOfRows = 0;

        Sheet receipts = wb.createSheet("Receipts");
        Sheet total = wb.createSheet("Total sold");
        Sheet days = wb.createSheet("Day stats");
        Sheet hours = wb.createSheet("Hour stats");

        receipts.setColumnWidth(0, (15 * 500));
        receipts.setColumnWidth(1, (15 * 500));
        receipts.setColumnWidth(2, (15 * 500));

        total.setColumnWidth(0, (15 * 500));
        total.setColumnWidth(1, (15 * 500));

        List<Item> itemsSold = new ArrayList<>();
        HashMap<String, List<Item>> mapDays = new HashMap<>();
        HashMap<String, List<Item>> mapHours = new HashMap<>();
        List<Item>[] hourStats = new List[48];
        long lTime = (long) Math.pow(10, 14);

        for (String receiptID : receiptsList) {
            File receiptFile = new File(getFilesDir(), receiptID);

            if (receiptFile != null) {
                BufferedReader reader = null;
                String line;
                try {
                    if (Long.parseLong(receiptID) < lTime)
                        lTime = Long.parseLong(receiptID);
                    String day = dayFormat.format(new Date(Long.parseLong(receiptID)));
                    String hour = hourFormat.format(new Date(Long.parseLong(receiptID)));
                    String[] hourData = hour.split(":");
                    int hourStatsID = Integer.parseInt(hourData[0]) * 2;
                    if (Integer.parseInt(hourData[1]) >= 30) hourStatsID++;

                    Row row = receipts.createRow(numOfRows);
                    numOfRows++;

                    c = row.createCell(0);
                    c.setCellValue("Saler name");
                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue("Time");
                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue("Total price");
                    c.setCellStyle(cs);

                    reader = new BufferedReader(new FileReader(receiptFile));
                    row = receipts.createRow(numOfRows);
                    numOfRows++;

                    line = reader.readLine();
                    String[] data = line.split(";");

                    c = row.createCell(0);
                    c.setCellValue(data[0]);
                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(simpleDateFormat.format(new Date(Long.parseLong(receiptID))));
                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(data[2]);
                    c.setCellStyle(cs);

                    row = receipts.createRow(numOfRows);
                    numOfRows++;

                    c = row.createCell(0);
                    c.setCellValue("Item name");
                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue("Quantity");
                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue("Price");
                    c.setCellStyle(cs);

                    while ((line = reader.readLine()) != null) {
                        String[] itemData = line.split(";");

                        row = receipts.createRow(numOfRows);
                        numOfRows++;

                        c = row.createCell(0);
                        c.setCellValue(itemData[0]);
                        c.setCellStyle(cs);

                        c = row.createCell(1);
                        c.setCellValue(itemData[3]);
                        c.setCellStyle(cs);

                        c = row.createCell(2);
                        c.setCellValue(itemData[1]);
                        c.setCellStyle(cs);

                        boolean isInList = false;
                        for (int i = 0; i < itemsSold.size(); i++) {
                            Item item = itemsSold.get(i);
                            if (item.getName().matches(itemData[0])) {
                                itemsSold.get(i).setQuantity(String.valueOf(Integer.parseInt(item.getQuantity()) + Integer.parseInt(itemData[3])));
                                isInList = true;
                            }
                        }
                        if (!isInList) {
                            itemsSold.add(new Item(itemData[0], itemData[3]));
                        }


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

                        if (hourStats[hourStatsID] == null){
                            hourStats[hourStatsID] = new ArrayList<>();
                            hourStats[hourStatsID].add(new Item(itemData[0], itemData[3], Long.parseLong(receiptID)));
                        }else {
                            boolean occured = false;
                            List<Item> items = hourStats[hourStatsID];
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
                            hourStats[hourStatsID] = items;
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    Log.e("error", "Unable to read file.");
                }
            }
        }

        int numOfRows2 = 0;
        Row row = total.createRow(numOfRows2);

        c = row.createCell(0);
        c.setCellValue("Product");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("Sold");
        c.setCellStyle(cs);

        for (Item item : itemsSold) {
            numOfRows2++;
            row = total.createRow(numOfRows2);

            c = row.createCell(0);
            c.setCellValue(item.getName());
            c.setCellStyle(cs);

            c = row.createCell(1);
            c.setCellValue(item.getQuantity());
            c.setCellStyle(cs);
        }

        int numOfRows3 = 0;
        int numOfRows4 = 0;
        row = days.createRow(numOfRows3);
        Row rowHour = hours.createRow(numOfRows4);
        HashMap<String, Integer> itemsColumn = new HashMap<>();

        c = row.createCell(0);
        c.setCellValue("Item/Day");
        c.setCellStyle(cs);

        days.setColumnWidth(0, (15 * 500));

        c = rowHour.createCell(0);
        c.setCellValue("Item/Hour");
        c.setCellStyle(cs);

        hours.setColumnWidth(0, (15 * 500));

        for (int i = 0; i < itemsSold.size(); i++) {
            c = row.createCell(i + 1);
            c.setCellValue(itemsSold.get(i).getName());
            c.setCellStyle(cs);

            days.setColumnWidth(i + 1, (15 * 500));

            c = rowHour.createCell(i + 1);
            c.setCellValue(itemsSold.get(i).getName());
            c.setCellStyle(cs);

            hours.setColumnWidth(0, (15 * 500));

            itemsColumn.put(itemsSold.get(i).getName(), i + 1);

        }

        int mapSize = mapDays.size();
        for (int i = 0; i < mapSize; i++){
            numOfRows3++;
            row = days.createRow(numOfRows3);

            long lowestTime = (long) Math.pow(10, 14);
            Map.Entry<String, List<Item>> lowestEntry = null;

            for (Map.Entry<String, List<Item>> entry : mapDays.entrySet()){
                if (lowestTime > entry.getValue().get(0).getTime()){
                    lowestTime = entry.getValue().get(0).getTime();
                    lowestEntry = entry;
                }
            }

            c = row.createCell(0);
            c.setCellValue(dateFormat.format(new Date(lowestEntry.getValue().get(0).getTime())));
            c.setCellStyle(cs);

            for (Item item : lowestEntry.getValue()){
                c = row.createCell(itemsColumn.get(item.getName()));
                c.setCellValue(item.getQuantity());
                c.setCellStyle(cs);
            }

            mapDays.entrySet().remove(lowestEntry);
        }

        for (List<Item> itemList : hourStats){

            if (itemList != null){
                numOfRows4++;
                rowHour = hours.createRow(numOfRows4);

                c = rowHour.createCell(0);
                String secondPart = "00";
                String[]hourData = hourFormat.format(new Date(itemList.get(0).getTime())).split(":");
                if (Integer.parseInt(hourData[1]) >= 30) secondPart = "30";
                c.setCellValue(hourData[0] + ":" + secondPart);
                c.setCellStyle(cs);

                for (Item item : itemList){
                    c = rowHour.createCell(itemsColumn.get(item.getName()));
                    c.setCellValue(item.getQuantity());
                    c.setCellStyle(cs);
                }
            }
        }

        long time = System.currentTimeMillis();
        FirebaseStorage.getInstance().getReference().child("Outputs").child(String.valueOf(time) + ".xlsx").putBytes(((HSSFWorkbook) wb).getBytes()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //task successful
                if (receiptsFile != null) receiptsFile.delete();
            }
        });

    }

    private void saveItemsToFile() {
        String strToFile = "";

        for (Item item : itemList) {
            strToFile += item.getName() + ";" + item.getPrice() + ";" + item.getImage() + ";" + item.getDiscount() + ";;" + item.getX() + ";" +
                    item.getY() + ";" + "\n";
        }

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput("items", Context.MODE_PRIVATE);
            outputStream.write(strToFile.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
