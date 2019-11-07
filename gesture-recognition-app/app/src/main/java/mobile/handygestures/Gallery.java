

package mobile.handygestures;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class Gallery extends AppCompatActivity {
 // This is the gallery activity that handles recognition of symbols based on loaded images from the gallery.
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE2 = 2;
    private Bitmap bitmap;
    private ImageView imageView, imageView2;


    private Recognition recognition;

    private TextView textView, textView1, textView2, textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gets links to all the required components and classes

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 250, 250);


        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        imageView2 = findViewById(R.id.imageView2);
        imageView2.setImageBitmap(bitmap);


        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);

        recognition = new Recognition(Gallery.this, textView);



    }



    public void classify(View view) {
        // Gets the predictions for the images and runs the command through the Recognition class
        int firstPrediction = recognition.classifyFrame(((BitmapDrawable) imageView.getDrawable()).getBitmap());
        int secondPrediction = recognition.classifyFrame(((BitmapDrawable) imageView2.getDrawable()).getBitmap());

        textView1.setText("" + recognition.idToLetter[firstPrediction]);// + " " +  Integer.toString(firstPrediction));
        textView2.setText("" + recognition.idToLetter[secondPrediction]);// + " " +  Integer.toString(secondPrediction));

        recognition.runCommand(firstPrediction, secondPrediction);


    }
    //  Runs the speak method from the Recognition class
    public void speakButton(View view) {
        recognition.speak(textView.getText().toString());
    }

    public void pickImage(View view) {
        // Allows the user pick an image from the gallery using the onActivityResult override.
        // Uses the button tag and request codes to determine where to load the image

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);


        String name = view.getTag().toString();
        Log.e("my", "cos" + name);
        if (Integer.parseInt(name) == 1) startActivityForResult(intent, REQUEST_CODE);
        if (Integer.parseInt(name) == 2) startActivityForResult(intent, REQUEST_CODE2);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE || requestCode == REQUEST_CODE2) && resultCode == Activity.RESULT_OK)
            try {
                // We need to recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bitmap, 250, 250);
                stream.close();

                if (requestCode == REQUEST_CODE )  {Log.e("my", "first");  imageView.setImageBitmap(bitmap2);}
                if (requestCode == REQUEST_CODE2 ) {Log.e("my", "sec"); imageView2.setImageBitmap(bitmap2);}

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
