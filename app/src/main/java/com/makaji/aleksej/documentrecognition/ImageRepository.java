package com.makaji.aleksej.documentrecognition;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;

import com.makaji.aleksej.documentrecognition.activity.MainActivity;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static com.makaji.aleksej.documentrecognition.R.id.loadingDataSet;
import static com.makaji.aleksej.documentrecognition.R.id.spinner;

/**
 * Created by Aleksej on 9/14/2017.
 */

@EBean
public class ImageRepository {

    private static final String DATASET_NAME = "dataset";

    private static final String TAG = "MainActivity";

    @RootContext
    Context context;

    @RootContext
    MainActivity mainActivity;

    ArrayList<String> listImages = new ArrayList<String>();

    ArrayList<Bitmap> listImagesBitmap = new ArrayList<Bitmap>();

    @AfterInject
    void init() {

        //Load all images
        getAllImages();

        //Convert image list to bitmap list
        getAllBitmaps();

    }

    /**
     * Get all images from specific folder in assets
     * @return List of images
     */
    public ArrayList<String> getAllImages() {

        String[] images = new String[0];

        try {
            images = context.getAssets().list(DATASET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        listImages = new ArrayList<>(Arrays.asList(images));

        return listImages;
    }

    /**
     * Draw image by position in a list
     * @param position Position in a list
     * @return Drawable image
     */
    public Drawable drawImageByPosition(Integer position) {

        InputStream inputstream = null;

        try {
            inputstream = context.getAssets().open(DATASET_NAME + "/" + listImages.get(position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Drawable drawable = Drawable.createFromStream(inputstream, null);

        return drawable;
    }

    public InputStream getImageInputstream(Integer position) {

        InputStream inputstream = null;

        try {
            inputstream = context.getAssets().open(DATASET_NAME + "/" + listImages.get(position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputstream;
    }

    /**
     * Get image by position and convert it to bitmap
     * @param position Position in a list
     * @return Image as bitmap
     */
    public Bitmap bitmapImage(Integer position) {

        InputStream inputstream = null;

        try {
            inputstream = context.getAssets().open(DATASET_NAME + "/" + listImages.get(position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputstream);

        return bitmap;
    }

    /**
     * Convert image list to bitmap list.
     */
    @Background
    public void getAllBitmaps() {

        TimingLogger timings = new TimingLogger(TAG, "countWhitePixels");
        timings.addSplit("Begin method getAllbitmaps");

        for (int i=0; i<listImages.size(); i++) {
            listImagesBitmap.add(bitmapImage(i));
        }

        timings.addSplit("End method getAllbitmaps");
        timings.dumpToLog();

        hideProgress();

    }

    /**
     * Method which triggers when background method converts image list into bitmap list.
     * When trigger happen, hide loading spinner.
     */
    @UiThread
    void hideProgress() {

        mainActivity.findViewById(spinner).setVisibility(View.GONE);

        mainActivity.findViewById(loadingDataSet).setVisibility(View.GONE);

        Log.d(TAG, "Finished datasetInit: " + listImagesBitmap.size());

    }

    public ArrayList<Bitmap> getListImagesBitmap() {
        return listImagesBitmap;
    }

    public ArrayList<String> getListImages() {
        return listImages;
    }
}
