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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.opencv.android.LoaderCallbackInterface.SUCCESS;
import static org.opencv.core.Core.countNonZero;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final Integer IMAGE_POSITION = 1;

    Integer imagePositionIterator = IMAGE_POSITION;

    private static final Float PERCENTAGE = 40.0f;

    private static final Integer TOLERANCE = 50;

    private Integer documents = 0;

    private final Object lock = new Object();

    Boolean isDocument = false;

    Mat imageMat;
    Mat imageMat2;

    private int blackPixels2 = 0;
    private int whitePixels2 = 0;

    private Lock lock2 = new ReentrantLock();

    @ViewById
    ImageView image1;

    // async loader of OpenCV4Android lib
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imageMat = new Mat();
                    imageMat2 = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Bean
    ImageRepository imageRepository = new ImageRepository();

    @AfterViews
    void init() {
        Drawable image = imageRepository.drawImageByPosition(IMAGE_POSITION);
        image1.setImageDrawable(image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, loaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(SUCCESS);
        }
    }

    @Click
    void thresholdImageButton() {
        thresholdImage(imagePositionIterator);
    }

    @Click
    void detectDocumentButton() {
        detectDocument(imagePositionIterator);
    }

    @Click
    void showNextImage() {
        if (imagePositionIterator < imageRepository.getAllImages().size()-1) {
            imagePositionIterator++;
        }
        Drawable image = imageRepository.drawImageByPosition(imagePositionIterator);
        image1.setImageDrawable(image);
    }

    @Click
    void showPreviouseImage() {
        if (imagePositionIterator > 0) {
            imagePositionIterator--;
        }
        Drawable image = imageRepository.drawImageByPosition(imagePositionIterator);
        image1.setImageDrawable(image);
    }

    @Click
    void allImages() {

        Log.d(TAG, "repository size: " + imageRepository.getAllImages().size());

        doBackground();

    }

    @Click
    void getWhitePixels() {
        countWhitePixelsFromThreshold(imagePositionIterator);
        Toast.makeText(getApplicationContext(), "Is document: " + isDocument, Toast.LENGTH_SHORT).show();
    }

    public void doBackground() {
        /*TimingLogger timings = new TimingLogger(TAG, "countWhitePixels");
        timings.addSplit("Begin method");*/

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();

        for (int i = 0; i < 1; i++) {
            //for (int i = 0; i < imageRepository.getAllImages().size(); i++) {
            countWhitePixelsFromThreshold(i);
            if (isDocument) {
                stringBuilder.append(imageRepository.getAllImages().get(i) + " ");
            } else {
                stringBuilder2.append(imageRepository.getAllImages().get(i) + " ");
            }
        }

        String finalString = stringBuilder.toString();
        String finalString2 = stringBuilder2.toString();

        Log.d(TAG, "Valid doucments are: " + finalString);
        Log.d(TAG, "NOT doucments are: " + finalString2);


        /*timings.addSplit("End method");
        timings.dumpToLog();*/
    }

    /**
     * Do threshold of image (convert image in binary, white and black pixels)
     *
     * @param imagePosition Position of image in a list
     */
    public void thresholdImage(Integer imagePosition) {

        TimingLogger timings = new TimingLogger(TAG, "countWhitePixels");
        timings.addSplit("Begin method");

        Bitmap bitmap = imageRepository.bitmapImage(imagePosition);

        timings.addSplit("Take item from array list");

        // first convert bitmap into OpenCV mat object
        imageMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(4));
        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(myBitmap, imageMat);

        // now convert to gray
        Mat grayMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(1));
        Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_RGB2GRAY, 1);

        // get the thresholded image
        Mat thresholdMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(1));
        Imgproc.threshold(grayMat, thresholdMat, 128, 255, Imgproc.THRESH_BINARY);

        timings.addSplit("Finished threshhold");


        // convert back to bitmap for displaying
        Bitmap resultBitmap = Bitmap.createBitmap(thresholdMat.cols(), thresholdMat.rows(), Bitmap.Config.ARGB_8888);
        thresholdMat.convertTo(thresholdMat, CvType.CV_8UC1);
        Utils.matToBitmap(thresholdMat, resultBitmap);

        Drawable newImage = new BitmapDrawable(resultBitmap);
        //It scales the image after use, so i used code above
        //Drawable newImage = new BitmapDrawable(getResources(), resultBitmap);

        image1.setImageDrawable(newImage);

        timings.addSplit("End method");
        timings.dumpToLog();
    }

    /**
     * Detect text regions
     *
     * @param mat
     */
    public void detectText(Mat mat) {
        //imageMat2 = new Mat();
        Imgproc.cvtColor(imageMat, imageMat2, Imgproc.COLOR_RGB2GRAY);
        Mat mRgba = mat;
        Mat mGray = imageMat2;

        //Scalar CONTOUR_COLOR = new Scalar(1, 255, 128, 0);
        Scalar CONTOUR_COLOR = new Scalar(255, 1, 35, 0);
        MatOfKeyPoint keyPoint = new MatOfKeyPoint();
        List<KeyPoint> listPoint = new ArrayList<>();
        KeyPoint kPoint = new KeyPoint();
        Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
        int rectanx1;
        int rectany1;
        int rectanx2;
        int rectany2;

        Scalar zeros = new Scalar(0, 0, 0);
        List<MatOfPoint> contour2 = new ArrayList<>();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Mat morByte = new Mat();
        Mat hierarchy = new Mat();

        Rect rectan3 = new Rect();
        int imgSize = mRgba.height() * mRgba.width();

        if (true) {
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.MSER);
            detector.detect(mGray, keyPoint);
            listPoint = keyPoint.toList();
            for (int ind = 0; ind < listPoint.size(); ++ind) {
                kPoint = listPoint.get(ind);
                rectanx1 = (int) (kPoint.pt.x - 0.5 * kPoint.size);
                rectany1 = (int) (kPoint.pt.y - 0.5 * kPoint.size);

                rectanx2 = (int) (kPoint.size);
                rectany2 = (int) (kPoint.size);
                if (rectanx1 <= 0) {
                    rectanx1 = 1;
                }
                if (rectany1 <= 0) {
                    rectany1 = 1;
                }
                if ((rectanx1 + rectanx2) > mGray.width()) {
                    rectanx2 = mGray.width() - rectanx1;
                }
                if ((rectany1 + rectany2) > mGray.height()) {
                    rectany2 = mGray.height() - rectany1;
                }
                Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
                Mat roi = new Mat(mask, rectant);
                roi.setTo(CONTOUR_COLOR);
            }
            Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_DILATE, kernel);
            Imgproc.findContours(morByte, contour2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
            for (int i = 0; i < contour2.size(); ++i) {
                rectan3 = Imgproc.boundingRect(contour2.get(i));
                if (rectan3.area() > 0.5 * imgSize || rectan3.area() < 100 || rectan3.width / rectan3.height < 2) {
                    Mat roi = new Mat(morByte, rectan3);
                    roi.setTo(zeros);
                } else {
                    Imgproc.rectangle(mRgba, rectan3.br(), rectan3.tl(), CONTOUR_COLOR);
                }
            }
        }
    }

    /**
     * Detect document
     *
     * @param imagePosition
     */
    public void detectDocument(Integer imagePosition) {
        Bitmap bitmap = imageRepository.bitmapImage(imagePosition);
        Utils.bitmapToMat(bitmap, imageMat);
        detectText(imageMat);
        Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
        Utils.matToBitmap(imageMat, newBitmap);
        image1.setImageBitmap(newBitmap);

    }

    /**
     * Count white pixels and check if that pixels are more then 40% all pixels
     *
     * @param imagePosition Position of image in a list
     */
    public void countWhitePixelsFromThreshold(Integer imagePosition) {

        TimingLogger timings = new TimingLogger(TAG, "countWhitePixels");
        timings.addSplit("Begin method");

        Bitmap bitmap = imageRepository.bitmapImage(imagePosition);

        timings.addSplit("Take item from array list");

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

        int blackPixels = 0;
        int whitePixels = 0;

        timings.addSplit("Before FOR");

        whitePixels = countNonZero(thresholdMat);
        blackPixels = thresholdMat.cols() * thresholdMat.rows() - whitePixels;

        Log.d(TAG, "Count white: " + whitePixels);
        Log.d(TAG, "Count black: " + blackPixels);

        timings.addSplit("After for");

        //check if white pixels are more then 40%
        int pixels = blackPixels + whitePixels;
        int pixels40percentage = (int) (pixels * (PERCENTAGE / 100.0f));

        if (pixels40percentage < whitePixels) {
            isDocument = true;
            documents++;
        } else {
            isDocument = false;
        }

        timings.addSplit("End method");
        timings.dumpToLog();
    }

}
