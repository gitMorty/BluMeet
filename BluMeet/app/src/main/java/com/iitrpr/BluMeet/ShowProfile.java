package com.iitrpr.BluMeet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ShowProfile extends Activity {

    ImageView img;
    TextView name;
    TextView aboutme;
    TextView emailAddress;
    TextView phoneNumber;

    Button yesButton;
    Button noButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iitrpr.BluMeet.R.layout.showprofile);

        Intent intent = getIntent();
        //int position = intent.getIntExtra("position", 0);

        img = (ImageView) findViewById(com.iitrpr.BluMeet.R.id.my_image);
        name = (TextView) findViewById(com.iitrpr.BluMeet.R.id.whole_name);
        aboutme = (TextView) findViewById(com.iitrpr.BluMeet.R.id.about_me);
        emailAddress = (TextView) findViewById(com.iitrpr.BluMeet.R.id.emailAddress);
        phoneNumber = (TextView) findViewById(com.iitrpr.BluMeet.R.id.phoneNumber);

        yesButton = (Button) findViewById(com.iitrpr.BluMeet.R.id.yes);
        noButton = (Button) findViewById(com.iitrpr.BluMeet.R.id.no);


        byte[] byteArray = intent.getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        img.setImageBitmap(bmp);

        String user_name = intent.getStringExtra("un");
        final String test_name = intent.getStringExtra("n");
        name.setText(intent.getStringExtra("n"));
        aboutme.setText(intent.getStringExtra("am"));

        final String email = intent.getStringExtra("em");
        final String phonenum = intent.getStringExtra("pn");

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

           /*Intent returnIntent = new Intent();
                returnIntent.putExtra("answer","no");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();*/

            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent returnIntent = new Intent();
                returnIntent.putExtra("answer","no");
                setResult(Activity.RESULT_OK,returnIntent);
                finish();*/
            }
        });


    }

}