package com.example.campus;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ItemAdd extends Activity {

    private TextView ItemName;
    private TextView ItemDesc;
    private TextView ItemTax;
    private TextView ItemDiscount;
    private TextView ItemPrice;
    private TextView ItemEAN;
    private int REQUEST_GET_SINGLE_FILE = 1;
    private byte[] imageData;
    private static final int CAMERA_REQUEST = 2;
    private ImageView imageView;
    private Bitmap finalBitmap;
    private long itemID = 0;
    private TextView action;
    private boolean isEdit = false;
    private int X;
    private int Y;

    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_add);

        imageView = findViewById(R.id.imageView);
        ItemName = findViewById(R.id.ItemName);
        ItemDesc = findViewById(R.id.ItemDesc);
        ItemPrice = findViewById(R.id.ItemPrice);
        ItemDiscount = findViewById(R.id.ItemDiscount);

        Button getImage;
        Button saveBut;
        Button itemPhoto;
        Button saveEan;

        //loads data from intent extras

        try {
            itemID = Long.parseLong(getIntent().getExtras().getString("itemID"));
            ItemName.setText(getIntent().getExtras().getString("name"));
            ItemDiscount.setText(getIntent().getExtras().getString("discount"));
            ItemPrice.setText(getIntent().getExtras().getString("price"));
            X = getIntent().getExtras().getInt("X");
            Y = getIntent().getExtras().getInt("Y");

            action = findViewById(R.id.AddItem_l);
            action.setText("Edit item");

            File directory = getApplicationContext().getDir("Images", Context.MODE_PRIVATE);
            File imageFile = new File(directory, itemID + ".png");
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(imageFile);
                imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
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
            isEdit = true;
        } catch (Exception e) {

        }

        //initializes bascode detector and camera source as source of image data

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.EAN_13 | Barcode.EAN_8).build();

        cameraSource = new CameraSource
                .Builder(getApplicationContext(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();

        getImage = findViewById(R.id.ItemImage);
        getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);
            }
        });
        itemPhoto = findViewById(R.id.ItemPhoto);
        itemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        //saves new item into local file

        saveBut = findViewById(R.id.SaveItem);
        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!ItemName.getText().toString().isEmpty() && isNumeric(ItemPrice.getText().toString()) &&
                        !ItemPrice.getText().toString().isEmpty()) {
                    if (itemID == 0) itemID = System.currentTimeMillis();

                    String strToFile = ItemName.getText().toString() + ";" + ItemPrice.getText().toString() + ";" + itemID + ";";
                    if (ItemDiscount.getText().toString().isEmpty()) strToFile += "0" + ";";
                    else strToFile += ItemDiscount.getText().toString() + ";";
                    if (ItemDesc.getText().toString().isEmpty()) strToFile += ";";
                    else strToFile += ItemDesc.getText().toString() + ";";

                    if (!isEdit) {
                        X = (Purchase.BUTTON_WIDTH * (ManageItems.numOfItems % 3)) + 10;
                        Y = (Purchase.BUTTON_HEIGTH * (ManageItems.numOfItems/3)) + 10;
                        strToFile += X + ";";
                        strToFile += Y + ";";
                        strToFile += "\n";

                        FileOutputStream outputStream;

                        try {
                            outputStream = openFileOutput("items", Context.MODE_APPEND);
                            outputStream.write(strToFile.getBytes());
                            outputStream.close();

                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        strToFile += X + ";";
                        strToFile += Y + ";";
                        strToFile += "\n";

                        File itemsFile = new File(getFilesDir(), "items");
                        String contentOfFile = "";

                        if (itemsFile != null) {
                            BufferedReader reader = null;
                            String line;
                            try {
                                reader = new BufferedReader(new FileReader(itemsFile));
                                while ((line = reader.readLine()) != null) {
                                    String[] itemData = line.split(";");
                                    if (String.valueOf(itemID).matches(itemData[2])) {
                                        contentOfFile += strToFile;
                                    } else {
                                        contentOfFile += line + "\n";
                                    }
                                }
                                reader.close();
                            } catch (Exception e) {
                                Log.e("error", "Unable to read file.");
                            }

                            FileOutputStream outputStream;

                            try {
                                outputStream = openFileOutput("items", Context.MODE_PRIVATE);
                                outputStream.write(contentOfFile.getBytes());
                                outputStream.close();

                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (finalBitmap != null) {
                        ContextWrapper cw = new ContextWrapper(getApplicationContext());
                        File directory = cw.getDir("Images", Context.MODE_PRIVATE);
                        File file = new File(directory, itemID + ".png");
                        if (!file.exists()) {
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();
                                fos.close();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else Toast.makeText(getApplicationContext(), "Missing data", Toast.LENGTH_SHORT).show();
            }
        });

        final SurfaceView cameraView = findViewById(R.id.camera_view);

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    if (ActivityCompat.checkSelfPermission(ItemAdd.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cameraSource != null) {
                    cameraSource.stop();
                }
            }
        });

        //starts scanning for EAN code

        saveEan = findViewById(R.id.saveItem);
        saveEan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.INVISIBLE);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //result of scanning

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    ItemEAN.setText(barcodes.valueAt(0).displayValue);
                    imageView.setVisibility(View.VISIBLE);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

    }

    //resize and creates bitmap from camera caption

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GET_SINGLE_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                imageData = baos.toByteArray();

                // Log.d(TAG, String.valueOf(bitmap));

                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            int imageHeight = photo.getHeight();
            int imageWidth = photo.getWidth();

            int shorterSide = imageWidth < imageHeight ? imageWidth : imageHeight;
            int longerSide = imageWidth < imageHeight ? imageHeight : imageWidth;
            boolean portrait = imageWidth < imageHeight;  //find out the image orientation
            //number array positions to allocate for one row of the pixels (+ some blanks - explained in the Bitmap.getPixels() documentation)
            int lengthToCrop = (longerSide - shorterSide) / 2; //number of pixel to remove from each side
            //size of the array to hold the pixels (amount of pixels) + (amount of strides after every line)
            int pixelArraySize = (shorterSide * shorterSide);
            int[] pixels = new int[pixelArraySize];

            photo.getPixels(pixels, 0, shorterSide, portrait ? 0 : lengthToCrop, portrait ? lengthToCrop : 0, shorterSide, shorterSide);

            Bitmap croppedBitmap = Bitmap.createBitmap(shorterSide, shorterSide, Bitmap.Config.ARGB_4444);
            croppedBitmap.setPixels(pixels, 0, shorterSide, 0, 0, shorterSide, shorterSide);

            finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 100, 100, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            imageData = baos.toByteArray();

            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(finalBitmap);
        }
    }
}

