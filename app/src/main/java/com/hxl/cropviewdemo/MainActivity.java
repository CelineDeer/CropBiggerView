package com.hxl.cropviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hxl.cropviewdemo.view.CropView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private CropView cropView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cropView = findViewById(R.id.cropView);

        cropView.setCropMode(CropView.CropModeEnum.FREE);
        cropView.setHandleShowMode(CropView.ShowModeEnum.NOT_SHOW);
        cropView.setGuideShowMode(CropView.ShowModeEnum.NOT_SHOW);

        InputStream is =getResources().openRawResource(R.raw.my_photo);
        Bitmap bitmap = BitmapFactory.decodeStream(is);


        if (bitmap != null){
            cropView.setImageBitmap(bitmap);
//            fax_bigger_view.setImageBitmap(originalBitmap);
        }

        cropView.setCenterCropMode(false);
    }

}