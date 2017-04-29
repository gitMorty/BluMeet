package com.iitrpr.BluMeet;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class HostActivity extends Activity {

    public static final int REQUEST_DISCOVERABLE = 1;
    public static final int PICK_IMAGE = 2;

    private String mUsername;
    private String mChatRoomName;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothSocket> mSockets;
    private AcceptThread mAcceptThread;

    private ChatManager mChatManager;

    String profileString;
    String imagePath;

    ArrayList<Integer> alreadySent;

    byte[] host_image;

    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iitrpr.BluMeet.R.layout.activity_chatroom);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profileString = getIntent().getStringExtra("profileString");
        imagePath = getIntent().getStringExtra("imagePath");

        Button mSendButton = (Button) findViewById(com.iitrpr.BluMeet.R.id.send);

        //new SendImageThread(imagePath, -1).start();  // here current_id not required
        host_image = getHostImagePath(imagePath);

        mChatManager = new ChatManager(this, true,profileString,host_image);

        alreadySent = new ArrayList<>();


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = mSockets.size();

                Log.e("send button host","came");
                for(int j=1;j<=size;j++) {
                    int current_id = j;

                    if(!alreadySent.contains(current_id)) {

                        for (int i = 0; i < mChatManager.storedMessages.size(); i++) {
                            if (mChatManager.storedIds.get(i) != current_id) {
                                String message = mChatManager.storedMessages.get(i);
                                byte[] image = mChatManager.storedImages.get(i);

                                sendMessage(message, current_id,1,null);
                                sendMessage("", current_id,2,image);
                                //new HostActivity.SendImageThread(new String(image), current_id).start();
                            }
                        }
                        alreadySent.add(current_id);
                    }

                }
            }
        });

        initializeRoom();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.iitrpr.BluMeet.R.menu.host, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else if (id == com.iitrpr.BluMeet.R.id.action_reopen) {
            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
            initializeBluetooth();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeRoom() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Retrieve username
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = sharedPref.getString("username", mBluetoothAdapter.getName());

        // Set up ChatRoom naming input
        /*final EditText nameInput = new EditText(this);
        nameInput.setSingleLine();
        nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Set up ChatRoom naming dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(com.iitrpr.BluMeet.R.string.enter_your_chatroom_name));
        builder.setView(nameInput);
        builder.setPositiveButton(getString(com.iitrpr.BluMeet.R.string.submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                mChatRoomName = nameInput.getText().toString();

                if (getActionBar() != null) {
                    getActionBar().setTitle(mChatRoomName);
                }

                imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                initializeBluetooth();
            }
        });
        builder.setNegativeButton(getString(com.iitrpr.BluMeet.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imm.hideSoftInputFromWindow(nameInput.getWindowToken(), 0);
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        // Show the dialog and disable the submit button until the name is longer than 0 characters
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });*/

        mChatRoomName = "BluMeet";

        if (getActionBar() != null) {
            getActionBar().setTitle(mChatRoomName);
        }

        initializeBluetooth();
    }

    private void initializeBluetooth() {
        mSockets = new ArrayList<>();

        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(i, REQUEST_DISCOVERABLE);
    }

    private void uploadAttachment() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(i, getString(com.iitrpr.BluMeet.R.string.select_picture)), PICK_IMAGE);
    }

    private void sendMessage(String temp,int current_id,int state, byte[] imgbytes) {
        byte[] byteArray = null;

        try {
            if(state==1) {
                byte[] messageBytes = temp.getBytes();
                byteArray = mChatManager.buildPacket(
                        ChatManager.MESSAGE_SEND,
                        mUsername,
                        messageBytes
                );
            }
            else if(state==2){
                byteArray = mChatManager.buildPacket(
                        ChatManager.MESSAGE_SEND_IMAGE,
                        mUsername,
                        imgbytes
                );
            }
        } catch (Exception e) {
            return;
        }

        mChatManager.writeMessage(byteArray,current_id,1);

    }

    /*private void sendImage(byte[] temp,int current_id) {
        byte[] byteArray;

        try {
            byte[] messageBytes = temp;
            byteArray = mChatManager.buildPacket(
                    ChatManager.MESSAGE_SEND_IMAGE,
                    mUsername,
                    messageBytes
            );
        } catch (Exception e) {
            return;
        }

        mChatManager.writeMessage(byteArray,current_id,1);

    }*/


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED && requestCode == REQUEST_DISCOVERABLE) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
            Toast.makeText(this, getString(com.iitrpr.BluMeet.R.string.searching_for_users), Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri image = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(image, filePathColumn, null, null, null);

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);

            //new SendImageThread(picturePath).start();
            cursor.close();
        } else if (requestCode == REQUEST_DISCOVERABLE) {
            Toast.makeText(this, getString(com.iitrpr.BluMeet.R.string.new_users_cannot_join_chatroom), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }

        if (mSockets != null) {
            for (BluetoothSocket socket : mSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close socket");
                    System.err.println(e.toString());
                }
            }
        }
    }

    private void manageSocket(BluetoothSocket socket) {
        mChatManager.startConnection(socket);
        mSockets.add(socket);

        byte[] byteArray;

        byteArray = mChatManager.buildPacket(
                ChatManager.MESSAGE_NAME,
                mUsername,
                mChatRoomName.getBytes()
        );

        Toast.makeText(this, "User connected", Toast.LENGTH_SHORT).show();
        mChatManager.writeChatRoomName(byteArray);
        Log.i("host","came till here");

    }

    byte[] getHostImagePath(String picturePath){
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

        if (bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024) {
            float scalingFactor;

            if (bitmap.getWidth() >= bitmap.getHeight()) {
                scalingFactor = 1024f / bitmap.getWidth();
            } else {
                Matrix fixRotation = new Matrix();
                fixRotation.postRotate(90);
                scalingFactor = 1024f / bitmap.getHeight();
            }

            bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (int) (bitmap.getWidth() * scalingFactor),
                    (int) (bitmap.getHeight() * scalingFactor),
                    false
            );
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, output);
        byte[] imageBytes = output.toByteArray();

        return imageBytes;
    }


    private class SendImageThread extends Thread {

        private Bitmap bitmap;

        int current_id;

        public SendImageThread(String picturePath, int cid) {
            this.bitmap = BitmapFactory.decodeFile(picturePath);
            current_id = cid;
        }

        public void run() {
            if (bitmap == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), getString(com.iitrpr.BluMeet.R.string.image_is_incompatible), Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }

            if (bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024) {
                float scalingFactor;

                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    scalingFactor = 1024f / bitmap.getWidth();
                } else {
                    Matrix fixRotation = new Matrix();
                    fixRotation.postRotate(90);
                    scalingFactor = 1024f / bitmap.getHeight();
                }

                bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        (int) (bitmap.getWidth() * scalingFactor),
                        (int) (bitmap.getHeight() * scalingFactor),
                        false
                );
            }

            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 15, output);
                byte[] imageBytes = output.toByteArray();

                host_image = imageBytes;

                byte[] packet = mChatManager.buildPacket(
                        ChatManager.MESSAGE_SEND_IMAGE,
                        mUsername,
                        imageBytes
                );
                return;
                //mChatManager.writeMessage(packet);
                //mChatManager.writeMessage(packet,current_id,1);
            } catch (Exception e) {
                System.err.println("Failed to send image");
                System.err.println(e.toString());
            }
        }

    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;
        private boolean isAccepting;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            isAccepting = true;

            try {
                tmp = mBluetoothAdapter.
                        listenUsingRfcommWithServiceRecord(
                                mChatRoomName, java.util.UUID.fromString(MainActivity.UUID)
                        );
            } catch (IOException e) {
                System.err.println("Failed to set up Accept Thread");
                System.err.println(e.toString());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            while (isAccepting) {
                final BluetoothSocket socket;

                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            manageSocket(socket);
                        }
                    });
                }
            }
        }

        public void cancel() {
            try {
                isAccepting = false;
                mmServerSocket.close();
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

    }

}
