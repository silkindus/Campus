package com.example.campus;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ShowReceipts extends Activity {

    private ListView receiptsListView;
    private ListView itemsListView;
    private List<String> receiptsList = new ArrayList<>();
    private List<String> IDsList = new ArrayList<>();
    private ConstraintLayout questionConstraint;
    private Button yes;
    private Button no;
    private Button money;
    private Button card;
    public static TextView totalPriceText;
    public static float totalPrice;
    private boolean payWithMoney = false;
    private TextView sellerName;
    public static List<Item> orderItemList = new ArrayList<>();
    public static ShowReceipts showReceipts;
    private int positionOfReceipt;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.MM.yyyy - HH:mm:ss");
    private Button deleteButton;
    private boolean fromCancel = false;
    private TextView sure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_receipts);

        showReceipts = this;

        receiptsListView = findViewById(R.id.receipts_list);
        itemsListView = findViewById(R.id.items_list);
        questionConstraint = findViewById(R.id.question_constraint);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        money = findViewById(R.id.money);
        card = findViewById(R.id.card);
        totalPriceText = findViewById(R.id.total_price);
        sellerName = findViewById(R.id.name);
        deleteButton = findViewById(R.id.delete);
        sure = findViewById(R.id.sure);

        simpleDateFormat.setTimeZone(TimeZone.getDefault());

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

        //stores the updated receipt into local file

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromCancel) deleteReceipt();
                else {
                    if (orderItemList.size() > 0) {
                        String receiptData = MainActivity.curSaler + ";" + payWithMoney + ";" + totalPrice + "\n";
                        for (Item item : orderItemList) {
                            receiptData += item.getName() + ";" + item.getPrice() + ";" + item.getImage() + ";" + item.getQuantity() + ";" +
                                    item.getDiscount() + "\n";
                        }

                        FileOutputStream outputStream;

                        try {

                            outputStream = openFileOutput(IDsList.get(positionOfReceipt), Context.MODE_PRIVATE);
                            outputStream.write(receiptData.getBytes());
                            outputStream.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        deleteReceipt();
                    }
                }
                    questionConstraint.setVisibility(View.GONE);
                }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                questionConstraint.setVisibility(View.GONE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromCancel = true;
                sure.setText("Are you sure - DELETE");
                questionConstraint.setVisibility(View.VISIBLE);
            }
        });

        loadData();

        //shows the contents of receipt after chosen from ListView

        receiptsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                orderItemList.clear();
                money.setVisibility(View.VISIBLE);
                card.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                positionOfReceipt = position;

                File receiptsFile = new File(getFilesDir(), String.valueOf(IDsList.get(position)));

                if (receiptsFile != null) {
                    BufferedReader reader = null;
                    String line;
                    try {
                        reader = new BufferedReader(new FileReader(receiptsFile));
                        line = reader.readLine();
                        String[] data = line.split(";");
                        sellerName.setText(data[0]);
                        if (data[1].matches("true")) payWithMoney = true;
                        else payWithMoney = false;
                        totalPrice = Float.parseFloat(data[2]);

                        while ((line = reader.readLine()) != null) {
                            data = line.split(";");
                            orderItemList.add(new Item(data[0], data[1], data[2], data[3], Float.parseFloat(data[4])));
                        }
                        makeListView();
                        reader.close();
                    } catch (Exception e) {
                        Log.e("error", "Unable to read file.");
                    }
                }
            }
        });
    }

    public void makeListView(){
        itemsListView.setAdapter(new OrderAdapter(getApplicationContext(), 1, orderItemList));
    }

    //loads all data
    private void loadData(){
        IDsList.clear();
        receiptsList.clear();

        File receiptsFile = new File(getFilesDir(), "receipts");

        if (receiptsFile != null) {
            BufferedReader reader = null;
            String line;
            try {
                reader = new BufferedReader(new FileReader(receiptsFile));
                while ((line = reader.readLine()) != null) {
                    IDsList.add(line);
                    receiptsList.add(simpleDateFormat.format(new Date(Long.parseLong(line))));
                }
                reader.close();
            } catch (Exception e) {
                Log.e("error", "Unable to read file.");
            }

            receiptsListView.setAdapter(new SalersAdapter(getApplicationContext(), 1, receiptsList));
        }
    }

    //deletes all data conneted with seleted receipt

    private void deleteReceipt(){
        File receiptFile = new File(getFilesDir(), IDsList.get(positionOfReceipt));
        receiptFile.delete();

        IDsList.remove(positionOfReceipt);
        String IDs = "";
        for (String ID : IDsList){
            IDs += ID + "\n";
        }
        FileOutputStream outputStream;

        try {

            outputStream = openFileOutput("receipts", Context.MODE_PRIVATE);
            outputStream.write(IDs.getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        orderItemList.clear();
        makeListView();
        money.setVisibility(View.INVISIBLE);
        card.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);
        totalPriceText.setText("0");

        loadData();
    }
}

