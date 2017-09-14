package com.makaji.aleksej.documentrecognition;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Aleksej on 9/14/2017.
 */

@EBean
public class ImageRepository {

    @RootContext
    Context context;

    /**
     * Get all images from dataset folder
     * @return List of images
     */
    public ArrayList<String> getAllImages() {

        String[] images = new String[0];

        try {
            images = context.getAssets().list("dataset");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> listImages = new ArrayList<String>(Arrays.asList(images));

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
            inputstream = context.getAssets().open("dataset/" + getAllImages().get(position));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Drawable drawable = Drawable.createFromStream(inputstream, null);

        return drawable;
    }


}
