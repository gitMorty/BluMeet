package com.iitrpr.BluMeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ChatManager {

    public static final int BODY_LENGTH_END = 255;
    public static final int BODY_LENGTH_END_SIGNED = -1;

    public static final int MESSAGE_ID = 1;
    public static final int MESSAGE_NAME = 2;
    public static final int MESSAGE_SEND = 3;
    public static final int MESSAGE_RECEIVE = 4;
    public static final int MESSAGE_SEND_IMAGE = 5;
    public static final int MESSAGE_RECEIVE_IMAGE = 6;

    private boolean isHost;
    private boolean isInitialized = false;
    private ArrayList<ConnectedThread> connections;
    private int id;

    private ArrayList<MessageBox> mMessageList;
    private MessageFeedAdapter mFeedAdapter;
    private ListView mMessageFeed;

    private Activity mActivity;
    private ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;


    ArrayList<String> storedMessages;
    ArrayList<byte[]> storedImages;
    ArrayList<Integer> storedIds;

    String profileStoring_untilImageComes;

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            byte[] packet = (byte[]) msg.obj;
            int senderLength = msg.arg1;
            int senderId = msg.arg2;

            String sender = new String(Arrays.copyOfRange(packet, 0, senderLength));
            byte[] body = Arrays.copyOfRange(packet, senderLength, packet.length);

            boolean isSelf = senderId == id;



            switch (msg.what) {
                case MESSAGE_ID:
                    id = body[0];
                case MESSAGE_NAME:
                    if (!isHost && !isInitialized) {
                        String chatRoomName = new String(body);

                        if (mActivity.getActionBar() != null) {
                            mActivity.getActionBar().setTitle(chatRoomName);
                        }

                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        mProgressDialog.dismiss();

                        Toast.makeText(mActivity, mActivity.getString(com.iitrpr.BluMeet.R.string.connected), Toast.LENGTH_SHORT).show();



                        isInitialized = true;
                    }
                    break;
                case MESSAGE_SEND:
                    if (isHost) {
                        byte[] sendPacket = buildPacket(MESSAGE_SEND, senderId, sender, body);
                        writeMessage(sendPacket, senderId,0);
                    }
                    break;
                case MESSAGE_RECEIVE:
                    if(!isSelf) {
                        //MessageBox messageBox = new MessageBox(sender, new String(body), new Date(), isSelf);
                        Log.i("host adding","id is "+id);
                        profileStoring_untilImageComes = new String(body);
                        //addMessage(messageBox);
                        if(isHost){
                            storedMessages.add(new String(body));
                            //storedIds.add(senderId);
                        }
                    }
                    else{
                        Log.i("isself",new Integer(id).toString());
                    }
                    break;
                case MESSAGE_SEND_IMAGE:
                    if (isHost) {
                        byte[] sendImagePacket = buildPacket(MESSAGE_SEND_IMAGE, senderId, sender, body);
                        writeMessage(sendImagePacket, senderId,0);
                    }
                    break;
                case MESSAGE_RECEIVE_IMAGE:
                    if(!isSelf) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(body, 0, body.length);
                        Log.i("recieved bitmaps ",new String(body));
                        MessageBox imageBox = new MessageBox(sender,profileStoring_untilImageComes, bitmap, new Date(), isSelf);
                        addMessage(imageBox);
                        if(isHost){
                            //storedMessages.add(new String(body));
                            storedImages.add(body);
                            storedIds.add(senderId);
                        }
                    }
                    else{
                        Log.i("isself",new Integer(id).toString());
                    }
            }
        }

    };

    public ChatManager(Activity activity, boolean isHost) {
        mActivity = activity;
        mMessageFeed = (ListView) mActivity.findViewById(com.iitrpr.BluMeet.R.id.message_feed);
        this.isHost = isHost;

        if (isHost) {
            id = 0;
            connections = new ArrayList<>();
            /*storedImages = new ArrayList<>();
            storedMessages = new ArrayList<>();
            storedIds = new ArrayList<>();
            */
        }

        mMessageList = new ArrayList<>();
        mFeedAdapter = new MessageFeedAdapter(mActivity, mMessageList);
        mMessageFeed.setAdapter(mFeedAdapter);

        View footer = new View(mActivity);
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250));
        footer.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));

        mMessageFeed.addFooterView(footer, null, false);
    }


    public ChatManager(Activity activity, boolean isHost,String ps,byte[] imgp) {
        mActivity = activity;
        mMessageFeed = (ListView) mActivity.findViewById(com.iitrpr.BluMeet.R.id.message_feed);
        this.isHost = isHost;

        if (isHost) {
            id = 0;
            connections = new ArrayList<>();
            storedImages = new ArrayList<>();
            storedMessages = new ArrayList<>();
            storedIds = new ArrayList<>();
            storedMessages.add(ps);
            storedImages.add(imgp);
            storedIds.add(0);
        }

        mMessageList = new ArrayList<>();
        mFeedAdapter = new MessageFeedAdapter(mActivity, mMessageList);
        mMessageFeed.setAdapter(mFeedAdapter);

        View footer = new View(mActivity);
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250));
        footer.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));

        mMessageFeed.addFooterView(footer, null, false);
    }



    public void startConnection(BluetoothSocket socket) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String username = sharedPref.getString("username", BluetoothAdapter.getDefaultAdapter().getName());
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        if (isHost) {
            connections.add(mConnectedThread);
            byte[] idAssignmentPacket = buildPacket(
                    MESSAGE_ID,
                    username,
                    new byte[] { (byte) connections.size() }
            );
            mConnectedThread.write(idAssignmentPacket);
        }
    }

    public void startConnection(BluetoothSocket socket, ProgressDialog progressDialog) {
        startConnection(socket);
        mProgressDialog = progressDialog;
    }

    public byte[] buildPacket(int type, int senderId, String sender, byte[] body) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(type);
        output.write(sender.length());

        int bodyLength = body.length;
        do {
           output.write(bodyLength % 10);
           bodyLength = bodyLength / 10;
        } while (bodyLength > 0);

        try {
            output.write(BODY_LENGTH_END);
            output.write(senderId);
            output.write(sender.getBytes());
            output.write(body);
        } catch (IOException e) {
            System.err.println("Error in building packet.");
            return null;
        }

        return output.toByteArray();
    }

    public byte[] buildPacket(int type, String sender, byte[] body) {
        return buildPacket(type, id, sender, body);
    }

    public void writeChatRoomName(byte[] byteArray) {
        connections.get(connections.size() - 1).write(byteArray);
    }

    public void writeMessage(byte[] byteArray, int senderId,int state) {
        int type = byteArray[0];
        int receiveType = 0;
        if (type == MESSAGE_SEND) {
            receiveType = MESSAGE_RECEIVE;
        } else if (type == MESSAGE_SEND_IMAGE) {
            receiveType = MESSAGE_RECEIVE_IMAGE;
        }

        Log.e("msgVal",new String(byteArray));
        int senderLength = byteArray[1];

        int currIndex = 2;
        do {
            currIndex++;
        } while (byteArray[currIndex] != BODY_LENGTH_END_SIGNED);

        if(state==0) mHandler.obtainMessage(receiveType, senderLength, senderId, Arrays.copyOfRange(byteArray, currIndex + 2, byteArray.length))
                .sendToTarget();

        if (isHost) {
            //Log.e("as host",new String(byteArray)+" "+new Integer(senderId).toString());
            if(state==0) new DistributeThread(receiveType, senderId, byteArray,state,-1).start();
            else if(state==1) new DistributeThread(receiveType, 0, byteArray,state,senderId).start();
        } else {
            mConnectedThread.write(byteArray);
        }
    }


    public void writeMessage(byte[] byteArray) {
        writeMessage(byteArray, id,0);
    }


    private void addMessage(MessageBox message) {
        int state=0;
        for(int i=0;i<mMessageList.size();i++){
            if(mMessageList.get(i).message.equals(message.message)){
                state=1;
                break;
            }
        }
        if(state==0) {
            mMessageList.add(message);
            mMessageFeed.invalidateViews();
            mFeedAdapter.notifyDataSetChanged();
            mMessageFeed.setSelection(mFeedAdapter.getCount() - 1);
        }
    }



    private class DistributeThread extends Thread {

        int mReceiveType;
        int mSenderId;
        private byte[] mByteArray;
        int toSendId;
        int state;

        public DistributeThread(int receiveType, int senderId, byte[] byteArray,int st,int tosid) {
            mReceiveType = receiveType;
            mSenderId = senderId;
            mByteArray = byteArray;
            toSendId=tosid;
            state=st;
        }



        public void run() {
            if(state==0) {
                mByteArray[0] = (byte) mReceiveType;
                for (int i = 0; i < connections.size(); i++) {
                    if (i + 1 != mSenderId) {
                        connections.get(i).write(mByteArray);
                    }
                }
            }
            else if(state==1){
                mByteArray[0] = (byte) mReceiveType;
                for (int i = 0; i < connections.size(); i++) {
                    if (i + 1 == toSendId) {
                        connections.get(i).write(mByteArray);
                    }
                }
            }
        }

    }

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(mActivity, mActivity.getString(com.iitrpr.BluMeet.R.string.could_not_connect_to_chatroom), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            while (true) {
                try {
                    int type = mmInStream.read();
                    int senderLength = mmInStream.read();

                    /*
                     * Calculate the length of the body in bytes. Each byte read is a digit
                     * in the body length in order of least to most significant digit.
                     *
                     * i.e. Body length of 247 would be read in the form {7, 4, 2}.
                     */
                    int bodyLength = 0;
                    int currPlace = 1;
                    int currDigit = mmInStream.read();
                    do {
                        bodyLength += (currDigit * currPlace);
                        currPlace *= 10;
                        currDigit = mmInStream.read();
                    } while (currDigit != BODY_LENGTH_END);

                    int senderId = mmInStream.read();

                    ByteArrayOutputStream packetStream = new ByteArrayOutputStream();
                    for (int i = 0; i < senderLength + bodyLength; i++) {
                        packetStream.write(mmInStream.read());
                    }
                    byte[] packet = packetStream.toByteArray();

                    mHandler.obtainMessage(type, senderLength, senderId, packet)
                            .sendToTarget();
                } catch (IOException e) {
                    System.err.println("Error in receiving packets");
                    e.printStackTrace();
                    endActivity();
                    break;
                }
            }
        }

        public void write(byte[] byteArray) {
            try {
                mmOutStream.write(byteArray);
                mmOutStream.flush();
            } catch (IOException e) {
                String byteArrayString = "";
                for (byte b : byteArray) {
                    byteArrayString += b + ", ";
                }
                System.err.println("Failed to write bytes: " + byteArrayString);
                System.err.println(e.toString());
                endActivity();
            }
        }

        private void endActivity() {
            if (!isHost) {
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mActivity, mActivity.getString(com.iitrpr.BluMeet.R.string.chatroom_closed), Toast.LENGTH_SHORT).show();
                        mActivity.finish();
                    }

                });
            }
        }

    }

}
