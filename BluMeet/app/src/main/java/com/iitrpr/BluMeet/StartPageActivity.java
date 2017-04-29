package com.iitrpr.BluMeet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.*;

public class StartPageActivity extends Activity {

    Button submit;
    Button selectPP;
    Button clearDetails;
    EditText username;
    EditText first_name;
    EditText last_name;
    EditText about_me;
    EditText phone_number;
    EditText email_Address;

    String imagePath;

    private static final int SELECT_PICTURE = 100;
    private static int RESULT_LOAD_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iitrpr.BluMeet.R.layout.activity_start_page);

        username = (EditText) findViewById(com.iitrpr.BluMeet.R.id.username);
        first_name = (EditText) findViewById(com.iitrpr.BluMeet.R.id.first_name);
        last_name = (EditText) findViewById(com.iitrpr.BluMeet.R.id.last_name);
        about_me = (EditText) findViewById(com.iitrpr.BluMeet.R.id.about_me);
        phone_number = (EditText) findViewById(com.iitrpr.BluMeet.R.id.phoneNumber);
        email_Address = (EditText) findViewById(com.iitrpr.BluMeet.R.id.emailID);
        selectPP = (Button) findViewById(com.iitrpr.BluMeet.R.id.select_image);
        clearDetails = (Button) findViewById(com.iitrpr.BluMeet.R.id.clear_my_details);

        String readString = readFromFile(getApplicationContext());

        if(!readString.equals("")){
            String readArr[] = readString.split("`");
            if(readArr.length == 7) {
                username.setText(readArr[0]);
                first_name.setText(readArr[1]);
                last_name.setText(readArr[2]);
                about_me.setText(readArr[3]);
                phone_number.setText(readArr[4]);
                email_Address.setText(readArr[5]);
                imagePath = readArr[6];
                selectPP.setText("Profile Picture selected");
            }
        }


        clearDetails.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                writeToFile("",view.getContext());
                username.setText("");
                first_name.setText("");
                last_name.setText("");
                about_me.setText("");
                phone_number.setText("");
                email_Address.setText("");
                imagePath = "";
                selectPP.setText("Select Profile Picture");
            }

        });


        selectPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
            }
        });



        submit = (Button) findViewById(com.iitrpr.BluMeet.R.id.submit);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                String un = username.getText().toString();
                String fn = first_name.getText().toString();
                String ln = last_name.getText().toString();
                String am = about_me.getText().toString();
                String pn = phone_number.getText().toString();
                String ea = email_Address.getText().toString();

                if(un!="" && un!=null && fn!="" && ln!="" && am!="" && pn!="" && ea!="" && fn!=null && ln!=null && am!=null && pn!=null && ea!=null && imagePath!=null && imagePath!=""){
                    String dataToStore1 = un+"`"+fn+"`"+ln+"`"+am+"`"+pn+"`"+ea;
                    String dataToStore2 = imagePath;
                    String dataToStore = dataToStore1+"`"+dataToStore2;

                    writeToFile(dataToStore,view.getContext());

                    Intent myIntent = new Intent(view.getContext(),MainActivity.class);
                    myIntent.putExtra("profileString",dataToStore1);
                    myIntent.putExtra("imagePath",dataToStore2);

                    startActivity(myIntent);
                }
                else{

                    Toast.makeText(getApplicationContext(), "Please enter all your details", Toast.LENGTH_SHORT).show();
                }


            }

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data)  {

                // Get the url from data
                Uri image = data.getData();

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(image, filePathColumn, null, null, null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                imagePath = picturePath;

                selectPP.setText("Profile Picture selected");

        }

    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("profileDetails.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            //String temp = StartPageActivity.this.getFilesDir().getAbsolutePath();
            //Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("profileDetails.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


}

