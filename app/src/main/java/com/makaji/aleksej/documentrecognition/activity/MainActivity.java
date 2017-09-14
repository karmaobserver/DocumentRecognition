package com.makaji.aleksej.documentrecognition.activity;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.makaji.aleksej.documentrecognition.ImageRepository;
import com.makaji.aleksej.documentrecognition.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById
    ImageView image1;

    @Bean
    ImageRepository imageRepository = new ImageRepository();

    @AfterViews
    void init() {

        Drawable image = imageRepository.drawImageByPosition(2);
        image1.setImageDrawable(image);

    }
}
