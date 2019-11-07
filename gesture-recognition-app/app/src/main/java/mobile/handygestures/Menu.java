package mobile.handygestures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {
// The menu activity handles the start menu with buttons to other activities and the quit option. This is the starting activity.
    Button galleryButton;
    Button helpButton;
    Button quitButton;
    Button previewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        galleryButton = (Button) findViewById(R.id.button_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this, Gallery.class));

            }
        });
        // make help button
        helpButton = (Button) findViewById(R.id.button_help);
        helpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this, Help.class));

            }
        });
        //make quit button
        quitButton = (Button) findViewById(R.id.button_quit);
        quitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);

            }
        });

        // make preview button
        previewButton = (Button) findViewById(R.id.button_preview);
        previewButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this, Preview.class));

            }
        });
    }
}
