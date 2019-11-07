package mobile.handygestures;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AutoDetection implements Runnable {

    private Mat tmpImg;
    private int pred1, pred2;
    private int prediction1, prediction2;
    private TextView txtv;

    private long startTime;

    private List<String> labels, prevLabels;
    private List<Float> probs, prevProbs; //previous labels

    private Recognition recognition;
    private Mat img;

    private ImageView imageView, imageView2;
    private TextView textView, textView1, textView2;

    final private Handler handler;
    private Preview preview;

    private Thread detector;

    public AutoDetection(Preview prv)
    {
        // Gets references to objects from Preview
        preview = prv;

        recognition = prv.recognition;
        img = preview.img;
        imageView = preview.imageView;
        imageView2 = preview.imageView2;
        textView = preview.textView;
        textView1 = preview.textView1;
        textView2 = preview.textView2;


        handler = new Handler();

        detector = new Thread(this);
    }

    public void begin() {
        // Starts the thread
        if (detector != null) {
            detector.start();
        }
    }

    public void stop() {
        // Stops the thread
        if (detector != null) {
            detector.interrupt();
        }
    }

    private int oneSign(int num, final ImageView imgv) {
        // Handles detection of one sign, by running the detection twice (probing and verification) and comparing the results.
        // It uses the detect method to find frames with potential signs and compareTo to verify that sign is stable. Also sets the interface objects of the detected frame.

        while (true) {
            if (num == 2 && System.currentTimeMillis() - startTime > 5000) return -1;
            sleep(500);


            pred1 = classify();
            if (pred1 != -1 && detect()) {


                sleep(300);


                tmpImg = img.clone();
                pred2 = classify();

                if (pred2 != -1 && detect()) {

                    if (compareLabels() < 500 && pred1 == pred2) {
                        handler.post(new Runnable() {
                            public void run() {
                                preview.setToBitmap(tmpImg, imgv);
                                txtv.setText("" + recognition.idToLetter[pred1]);
                            }
                        });


                        recognition.vibrate(100);
                        return pred1;
                    }
                }
            }
        }
    }



    @Override
    public void run() {
        // Runs the detection of the two signs and executes the commands (using Recognition),
        // handles timeout for second sign

        while (true) {
            txtv = textView1;
            prediction1 = oneSign(1, imageView);
            sleep(250);
            txtv = textView2;
            startTime = System.currentTimeMillis();
            prediction2 = oneSign(2, imageView2);
            if (prediction2 == -1) {
                resetInterface();
                continue;
            }

            handler.post(new Runnable() {
                public void run() {
                    recognition.runCommand(prediction1, prediction2);

                }
            });
            sleep(300);
            resetInterface();

        }
    }


    private void resetInterface() {
        // Resets the interface objects

        handler.post(new Runnable() {
            public void run() {
                imageView.setImageResource(R.drawable.logo);
                imageView2.setImageResource(R.drawable.logo);
                textView1.setText("");
                textView2.setText("");
            }
        });

    }


    private void sleep(int time)
    {
        // Sleeps the thread for some time
        try {
            Thread.sleep(time);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean detect()
    {
        // Checks if the highest confidence is above a threshold (hand in view)
        if (probs == null || probs.size() <= 0) return false;
        //Log.e("my", "Best prob " + Float.toString(probs.get(probs.size() -1)));
        if(probs.get(probs.size() -1) > 0.6) return true;
        else return false;
    }


    private int classify() {
        // Classifes the image and updates the labels
        int prediction = -1;
        if (img !=null && img.cols() > 0)
        {
            Bitmap bmp = getBitmapFromMat();
            prediction = recognition.classifyFrame(bmp);
            updateSortedLabels();
        }
        if (labels != null)
        {
            prevLabels = new ArrayList<String>(labels);
            prevProbs = new ArrayList<Float> (probs);
        }

        return prediction;

    }

    private int compareLabels()
    {
        // Returns a (heuristic) comparison value of two sets of predictions,
        // higher usually means less similar frames, use for verification of sign
        float sum = 0;

        final int size = prevLabels.size();
        for (int i = 0; i < size; i++)
        {
            sum += Math.abs(prevProbs.get(i) - probs.get(i));
            if (! prevLabels.get(i).equals(labels.get(i)) ) {sum += 1.0f;}
        }

        return  (int) (sum*100);

    }

    private Bitmap getBitmapFromMat()
    {
        // Returns a bitmap from Mat
        Mat tmpImg = img;
        Core.rotate(tmpImg, tmpImg, Core.ROTATE_90_CLOCKWISE); //ROTATE_180 or ROTATE_90_
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(tmpImg.cols(), tmpImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmpImg, bmp);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}

        bmp = ThumbnailUtils.extractThumbnail(bmp, 100, 100);

        return bmp;
    }

    private void updateSortedLabels()
    {
        // Updates the sorted array of labels with probabilities (from the latest prediction of ImageClassifer)
        // and splits them into labels and probabilities arrays
        PriorityQueue<Map.Entry<String, Float>> sortedLabels = recognition.getClassifier().getLabelsList();

        if (sortedLabels == null) return;

        labels = new ArrayList<>();
        probs = new ArrayList<>();

        final int size = sortedLabels.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            labels.add(label.getKey());
            probs.add(label.getValue());
        }

    }
}
