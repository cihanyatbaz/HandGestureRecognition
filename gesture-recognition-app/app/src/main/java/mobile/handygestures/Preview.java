package mobile.handygestures;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class Preview extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
 // This is the preview activity that handles recognition of symbols based on a camera stream.
    private static final String TAG = "my";

    protected CameraBridgeViewBase mOpenCvCameraView;
    private int mCameraId = 1;

    protected Mat img;
    protected ImageView imageView, imageView2;
    protected TextView textView, textView1, textView2;
    protected Recognition recognition;

    private int firstPrediction, secondPrediction;
    protected boolean autoOn = false;
    private Button analyzeButton;

    private AutoDetection autoDetect;

    // Loads OpenCV
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Preview() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Sets up the preview and links to other objects

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_preview);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setCameraIndex(mCameraId);

        imageView = findViewById(R.id.imageView5);
        imageView.setImageResource(R.drawable.logo);

        imageView2 = findViewById(R.id.imageView4);
        imageView2.setImageResource(R.drawable.logo);

        textView = findViewById(R.id.textView5);
        textView1 = findViewById(R.id.textView6);
        textView2 = findViewById(R.id.textView7);

        analyzeButton = findViewById(R.id.button5);

        recognition = new Recognition(Preview.this, textView);

    }



    public void startAuto(View view)
    {
        // Handles button to toggle automatic detection
        // (renames the button to stop and hides the manual detection button)

        Button b = (Button) view;
        if (autoOn)
        {
            b.setText("Auto \n start");
            analyzeButton.setVisibility(View.VISIBLE);
            autoOn = false;
            autoDetect.stop();
        }
        else
        {
            b.setText("Auto \n stop");
            analyzeButton.setVisibility(View.GONE);
            autoOn = true;
            autoDetect = new AutoDetection(this);
            autoDetect.begin();
        }

    }



    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Returns the frame image

        Mat tmpImg = inputFrame.rgba();

        if (mCameraId == 1) Core.flip(tmpImg, tmpImg, 1); //flip image mirror
        img = tmpImg;
        return img;
    }

    public void setToBitmap(Mat tmpImg, ImageView imgv)
    {
        // Sets the Mat image to a bitmap (correctly rotated)
        Core.rotate(tmpImg, tmpImg, Core.ROTATE_90_CLOCKWISE); //ROTATE_180 or ROTATE_90_
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(tmpImg.cols(), tmpImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmpImg, bmp);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}

        bmp = ThumbnailUtils.extractThumbnail(bmp, 224, 224);

        if (imgv != null) imgv.setImageBitmap(bmp);
    }

    public void showFrame(View view)
    {
        // Manual start of the detection (2s between signs)

        classifyFirst();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                classifySecond();
            }
        }, 2000);


    }

    private void classifyFirst() {
        // Classify the first sign (use Recognition class) and set the first image view
        setToBitmap(img, imageView);
        firstPrediction = recognition.classifyFrame(((BitmapDrawable) imageView.getDrawable()).getBitmap());
        textView1.setText("" + recognition.idToLetter[firstPrediction]);
        recognition.vibrate(100);
    }

    private void classifySecond() {
        // Classify the second sign, set the second image view and run the command
        setToBitmap(img, imageView2);
        secondPrediction = recognition.classifyFrame(((BitmapDrawable) imageView2.getDrawable()).getBitmap());
        Log.e("my", "f " + Integer.toString(firstPrediction) +" s " +  Integer.toString(secondPrediction));
        textView2.setText("" + recognition.idToLetter[secondPrediction]);
        recognition.vibrate(100);
        recognition.runCommand(firstPrediction, secondPrediction);


    }


    public void swapCamera(View view) {
        // Swaps the camera between front and back
        mCameraId = mCameraId^1;
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(mCameraId);
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        autoDetect.stop();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        autoDetect.stop();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }




}