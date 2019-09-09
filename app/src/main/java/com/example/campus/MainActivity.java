package com.example.campus;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ListView salersList;
    private List<String> salers = new ArrayList<>();
    private List<Integer> imageID = new ArrayList<>();
    public static String curSaler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        salersList = findViewById(R.id.salers_list);

        //initializes Firebase into app

        FirebaseApp.initializeApp(getApplicationContext());

        //creates list of sellers

        salers.add("1. saler");
        salers.add("2. saler");
        salers.add("3. saler");

        imageID.add(R.drawable.facepalm);
        imageID.add(R.drawable.smile);
        imageID.add(R.drawable.notbad);

        salersList.setAdapter(new SalersAdapter(getApplicationContext(), 0, salers, imageID));

        //listener on item click and after click moves into another activity

        salersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                curSaler = salers.get(position);

                Intent intent = new Intent(MainActivity.this, Purchase.class);
                startActivity(intent);
            }
        });
    }
}

