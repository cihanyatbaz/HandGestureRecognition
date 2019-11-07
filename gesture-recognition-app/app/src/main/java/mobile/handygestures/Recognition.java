package mobile.handygestures;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class Recognition {
    // This class handles the recognition and execution of the commands based on a pair of images.

    private ImageClassifier classifier;
    private TextToSpeech t1;
    private char[] commandList;
    public char[] idToLetter;

    private Activity activity;

    TextView textView;


    public Recognition(Activity activ, TextView txtv)
    {
        activity = activ;
        textView  = txtv;
        try {
            // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
            //      classifier = new ImageClassifierQuantizedMobileNet(getActivity());
            classifier = new ImageClassifierFloatInception(activity);
        } catch (IOException e) {
            Log.e("my", "Failed to initialize an image classifier.", e);
        }


        t1 = new TextToSpeech(activity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                    // t1.setPitch(1.0f);
                }
            }
        });

        commandList = new char[36];
        makeCommandList();
        idToLetter = new char[]{'A', 'B', 'C', 'L', 'T', 'W'};
    }


    public void vibrate(int time) {
        // Vibrates for a specific am mount of time
        Vibrator v = (Vibrator) activity.getSystemService (Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(time);
        }
    }


    private void putLetter(char letter) {
        // Adds a letter to the text view
        String oldText = textView.getText().toString();
        textView.setText(oldText + letter);
    }


    public void speak(String toSpeak) {
        // Vocalizes the text in the text view
        Toast.makeText(activity.getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }




    private void executeCommand(char a) {
        // Executes the command based on the char command

        String text = textView.getText().toString();

        if (a == '^' && text.length() > 0)
            textView.setText(text.substring(0, text.length() - 1));
        else if (a == '%')
            textView.setText("");
        else if (a == '@')
            speak(text);
        else if (a != '#')
            putLetter(a);
    }


    private int getPredictionID(char c) {
        // Maps letters to IDs
        if (c == 'A') return 0;
        if (c == 'B') return 1;
        if (c == 'C') return 2;
        if (c == 'L') return 3;
        if (c == 'T') return 4;
        if (c == 'W') return 5;
        return 0;
    }


    private void makeCommandList() {
        // Maps two prediction IDs to char command
        int indx = 0;
        for (int i = 0; i < 26; i++) commandList[indx++] = (char) (i + 65);
        for (int i = 0; i < 4; i++) commandList[indx++] = '#';
        commandList[indx++] = ' ';
        commandList[indx++] = '^';  // backspace
        commandList[indx++] = '%';  // clear
        commandList[indx++] = '@';  // speak
        for (int i = 0; i < 2; i++) commandList[indx++] = '#';
    }


    public void runCommand(int a, int b) {
        // Public start method of the command execution
        if (a >= 0 && b >= 0) {
            char command = getCommand(a, b);
            Log.e("my", "\ncomm: " + command);
            vibrate(150);
            executeCommand(command);
        }
    }



    public int classifyFrame(Bitmap bit) {
        // Extracts a thumbnail from the bitmap and runs the classifier on it
        if (classifier == null || activity == null) {
            Log.e("my", "Uninitialized Classifier or invalid context.");
            return 0;
        }
        SpannableStringBuilder textToShow = new SpannableStringBuilder();

        Bitmap originalBitmap = bit;

        Bitmap bitmap = ThumbnailUtils.extractThumbnail(originalBitmap, 224, 224);


        classifier.classifyFrame(bitmap, textToShow);
        bitmap.recycle();

       // Log.e("my", "\n" + textToShow.toString());
        //Toast.makeText(this, textToShow.toString(), Toast.LENGTH_SHORT).show();


        return getPredictionID(textToShow.toString().charAt(0));


    }
    // Returns the char command from prediction IDs
    private char getCommand(int a, int b) {
        return commandList[a * 6 + b];
    }

    public ImageClassifier getClassifier() {return classifier;}
}



/*
Sign tree (# is inactive):

A A - A
A B - B
A C - C
A L - D
A T - E
A W - F

B A - G
B B - H
B C - I
B L - J
B T - K
B W - L

C A - M
C B - N
C C - O
C L - P
C T - Q
C W - R

L A - S
L B - T
L C - U
L L - V
L T - W
L W - X

L A - Y
L B - Z
L C - #
L L - #
L T - #
L W - #

L A - SPACE
L B - BACKSPACE
L C - CLEAR
L L - SPEAK
L T - #
L W - #

 */