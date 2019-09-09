package com.example.campus;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageItems extends Activity {

    private Button itemAdd;
    private List<Item> itemList = new ArrayList<>();
    private GridView gridView;
    private TextView search;
    private EditText searchEdit;
    private Button find;
    public static int numOfItems;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        //creates context menu for GridView
        if (v.getId() == R.id.item_grid) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            String[] menuItems = getResources().getStringArray(R.array.manage_items_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        int menuItemIndex = menuItem.getItemId();
        int positionInGridView = info.position;

        //remove item from saved items
        if (menuItemIndex == 0) {

            //delete image from storage
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("Images", Context.MODE_PRIVATE);
            File file = new File(directory, itemList.get(positionInGridView).getImage() + ".png");
            if (file.exists()) file.delete();

            itemList.remove(positionInGridView);
            numOfItems = itemList.size();

            //removes item from all items records

            String strToFile = "";

            for (Item item : itemList){
                strToFile += item.getName() + ";" + item.getPrice() + ";" + item.getImage() + ";" + item.getDiscount() + ";;" + item.getX() + ";" +
                        item.getY() + ";" + "\n";
            }

            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput("items", Context.MODE_PRIVATE);
                outputStream.write(strToFile.getBytes());
                outputStream.close();

                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        makeGridView();

        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();

        itemList.clear();
        loadData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_items);

        itemAdd = findViewById(R.id.item_add);
        gridView = findViewById(R.id.item_grid);
        search = findViewById(R.id.search);
        searchEdit = findViewById(R.id.search_edit);
        find = findViewById(R.id.find);

        registerForContextMenu(gridView);

        loadData();

        itemAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageItems.this, ItemAdd.class);
                startActivity(intent);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //after click on Grid move to another activity with extras

                Intent intent = new Intent(ManageItems.this, ItemAdd.class);
                intent.putExtra("itemID", itemList.get(position).getImage());
                intent.putExtra("name", itemList.get(position).getName());
                intent.putExtra("price", itemList.get(position).getPrice());
                intent.putExtra("discount", String.valueOf(itemList.get(position).getDiscount()));
                intent.putExtra("X", itemList.get(position).getX());
                intent.putExtra("Y", itemList.get(position).getY());

                startActivity(intent);
            }
        });
    }

    private void loadData() {
        //loads all the data into data strucures needed
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
            numOfItems = itemList.size();

            makeGridView();
        }
    }

    private void makeGridView() {
        gridView.setAdapter(new GridViewAdapter(getApplicationContext(), itemList));
    }
}

