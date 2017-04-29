package com.iitrpr.BluMeet;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final String UUID = "28286a80-137b-11e4-bbe8-0002a5d5c51b";

    String profileString;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iitrpr.BluMeet.R.layout.activity_main);

        Button mHostButton = (Button) findViewById(com.iitrpr.BluMeet.R.id.host_button);
        Button mJoinButton = (Button) findViewById(com.iitrpr.BluMeet.R.id.join_button);

        profileString = getIntent().getStringExtra("profileString");
        imagePath = getIntent().getStringExtra("imagePath");

        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostRoom();
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinRoom();
            }
        });
    }

    private void hostRoom() {
        Intent i = new Intent(this, HostActivity.class);
        i.putExtra("profileString",profileString);
        i.putExtra("imagePath",imagePath);
        startActivity(i);
    }

    private void joinRoom() {
        Intent i = new Intent(this, ClientActivity.class);
        i.putExtra("profileString",profileString);
        i.putExtra("imagePath",imagePath);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.iitrpr.BluMeet.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == com.iitrpr.BluMeet.R.id.action_edit_name) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String username = sharedPref.getString("username", bluetoothAdapter.getName());
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            final EditText nameInput = new EditText(this);
            nameInput.setSingleLine();
            nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
            nameInput.setText(username);
            nameInput.setSelectAllOnFocus(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(com.iitrpr.BluMeet.R.string.enter_your_username));
            builder.setView(nameInput);
            builder.setPositiveButton(getString(com.iitrpr.BluMeet.R.string.submit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                    sharedPref.edit().putString(getString(com.iitrpr.BluMeet.R.string.username), nameInput.getText().toString()).apply();
                }
            });
            builder.setNegativeButton(getString(com.iitrpr.BluMeet.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                }
            });

            final AlertDialog dialog = builder.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            nameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    if (charSequence.length() > 0 && charSequence.length() <= 22) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(com.iitrpr.BluMeet.R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
        }
    }

}
