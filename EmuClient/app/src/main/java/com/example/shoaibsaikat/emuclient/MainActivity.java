package com.example.shoaibsaikat.emuclient;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity implements View.OnClickListener, ImageData {
    private Button mButton;
    private Client myClient = null;
    private ImageView mImageView;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mEditText = findViewById(R.id.editText);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if(myClient == null) {
                    myClient = new Client(this, "192.168.43.22", 7792);
                    myClient.start();
                    mButton.setText("Send");
                } else {
                    Bitmap bitmap;
                    byte[] byteArray;

                    View v1 = getWindow().getDecorView().getRootView();
                    v1.setDrawingCacheEnabled(true);
                    bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                    v1.setDrawingCacheEnabled(false);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    bitmap.recycle();

                    mImageView.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
                    myClient.sendMessage(byteArray);
                }
                break;
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory() + "/Download");
        if(!direct.exists()) {
            File wallpaperDirectory = new File("sdcard/Download/");
            wallpaperDirectory.mkdirs();
        }
        File file = new File(new File("/sdcard/Download/"), fileName);
        if(file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawImage(final byte[] image, final int length) {
        //createDirectoryAndSaveFile(BitmapFactory.decodeByteArray(image, 0, length), "echo.jpeg");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, length));
            }
        });
    }
}
