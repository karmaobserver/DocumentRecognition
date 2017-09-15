package com.makaji.aleksej.documentrecognition.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TimingLogger;
import android.widget.ImageView;
import android.widget.Toast;

import com.makaji.aleksej.documentrecognition.ImageRepository;
import com.makaji.aleksej.documentrecognition.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final Integer IMAGE_POSITION = 1;

    private static final Float PERCENTAGE = 40.0f;

    private static final Integer TOLERANCE = 50;

    Integer documents = 0;

    Boolean isDocument = false;

    Integer position = 1;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded successful");
        }
    }

    @ViewById
    ImageView image1;

    @Bean
    ImageRepository imageRepository = new ImageRepository();

    @AfterViews
    void init() {

        Drawable image = imageRepository.drawImageByPosition(IMAGE_POSITION);
        image1.setImageDrawable(image);
    }

    @Click
    void thresholdImageButton() {
        thresholdImage(147);
    }

    @Click
    void nextImage() {

        TimingLogger timings = new TimingLogger(TAG, "countWhitePixels");
        timings.addSplit("Begin method");
        countWhitePixels(position);
        timings.addSplit("End method");
        timings.dumpToLog();

        Log.d(TAG, "Is document: " + isDocument);

        position++;

        Log.d(TAG, "doucments: " + documents);

        Toast.makeText(getApplicationContext(), "Is document: " + isDocument, Toast.LENGTH_SHORT).show();

    }

    /**
     * Do trashhold of image (convert image in binary, white and black pixels)
     *
     * @param imagePosition Position of image in a list
     */
    public void thresholdImage(Integer imagePosition) {

        Bitmap bitmap = imageRepository.bitmapImage(imagePosition);

        // first convert bitmap into OpenCV mat object
        Mat imageMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(4));
        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(myBitmap, imageMat);

        // now convert to gray
        Mat grayMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(1));
        Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_RGB2GRAY, 1);

        // get the thresholded image
        Mat thresholdMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(1));
        Imgproc.threshold(grayMat, thresholdMat, 128, 255, Imgproc.THRESH_BINARY);

        // convert back to bitmap for displaying
        Bitmap resultBitmap = Bitmap.createBitmap(thresholdMat.cols(), thresholdMat.rows(), Bitmap.Config.ARGB_8888);
        thresholdMat.convertTo(thresholdMat, CvType.CV_8UC1);
        Utils.matToBitmap(thresholdMat, resultBitmap);

        Drawable newImage = new BitmapDrawable(resultBitmap);
        //It scales the image after use, so i used code above
        //Drawable newImage = new BitmapDrawable(getResources(), resultBitmap);

        image1.setImageDrawable(newImage);
    }

    /**
     * Count white pixels and check if that pixels are more then 40% all pixels
     *
     * @param imagePosition Position of image in a list
     */
    @Background
    public void countWhitePixels(Integer imagePosition) {
        Bitmap bitmap = imageRepository.bitmapImage(imagePosition);
        // first convert bitmap into OpenCV mat object
        Mat imageMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(4));
        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(myBitmap, imageMat);

        int blackPixels = 0;
        int whitePixels = 0;

        //x and y are changed in mat
        for (int y = 0; y < imageMat.rows(); y++) {
            for (int x = 0; x < imageMat.cols(); x++) {
                double[] pixel = imageMat.get(y, x);

                //Check pixel tolerance
                if ((pixel[0] <= 255 - TOLERANCE) && (pixel[1] <= 255 - TOLERANCE) && (pixel[2] <= 255 - TOLERANCE)) {
                    blackPixels++;
                } else {
                    whitePixels++;
                }
            }
        }
        //check if white pixels are more then 40%
        int pixels = blackPixels + whitePixels;
        int pixels40percentage = (int) (pixels * (PERCENTAGE / 100.0f));
        if (pixels40percentage < whitePixels) {
            isDocument = true;
            documents++;
        } else {
            isDocument = false;
        }
    }
}
