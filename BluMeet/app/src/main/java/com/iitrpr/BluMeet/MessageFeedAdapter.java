package com.iitrpr.BluMeet;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MessageFeedAdapter extends ArrayAdapter<MessageBox> {

    Context mContext;
    ClipboardManager clipboard;

    public MessageFeedAdapter(Context context, ArrayList<MessageBox> messages) {
        super(context, com.iitrpr.BluMeet.R.layout.message_row, messages);

        mContext = context;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MessageBox message = getItem(position);


        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(com.iitrpr.BluMeet.R.layout.message_row, parent, false);
        }

        TextView user_name = (TextView) convertView.findViewById(com.iitrpr.BluMeet.R.id.username);
        TextView timeView = (TextView) convertView.findViewById(com.iitrpr.BluMeet.R.id.time);
        ImageView imageView = (ImageView) convertView.findViewById(com.iitrpr.BluMeet.R.id.image);

        /*if (message.isSelf()) {
            senderView.setGravity(Gravity.END);
            messageView.setGravity(Gravity.END);

            RelativeLayout.LayoutParams rightAlign =
                    new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
            rightAlign.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            imageView.setLayoutParams(rightAlign);
        } else {
            senderView.setGravity(Gravity.START);
            messageView.setGravity(Gravity.START);

            RelativeLayout.LayoutParams leftAlign =
                    new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
            leftAlign.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageView.setLayoutParams(leftAlign);
        }*/

        /*if (!message.isImage()) {
            messageView.setText(message.getMessage());
            imageView.setImageDrawable(null);
        } else {
            messageView.setText("");
            imageView.setImageBitmap(message.getImage());
        }*/

        String msg_details = message.message;
        Bitmap img = message.getImage();

        if(msg_details==null){
            Toast.makeText(mContext, "MSG is null", Toast.LENGTH_SHORT).show();
            Log.i("mesNull","yes");
            if(img==null) {
                Toast.makeText(mContext, "IMG is null", Toast.LENGTH_SHORT).show();
                Log.i("Img","yes");
            }
            return convertView;
        }


        String details[] = msg_details.split("`");
        final String u_name = details[0];
        final String firstname = details[1];
        final String lastname = details[2];
        final String aboutme = details[3];
        final String phonenumber = details[4];
        final String emailaddress = details[5];


        user_name.setText(u_name);
        imageView.setImageBitmap(img);
        timeView.setText(message.getTime());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] byteArray = stream.toByteArray();


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),ShowProfile.class);

                intent.putExtra("image",byteArray);
                intent.putExtra("un", u_name);
                intent.putExtra("n", firstname+" "+lastname);
                intent.putExtra("am", aboutme);
                intent.putExtra("pn", phonenumber);
                intent.putExtra("em", emailaddress);
                ((Activity)mContext).startActivityForResult(intent,1);
            }
        });


        /*if (!message.isImage() && message.getMessage().length() > 0) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),ShowProfile.class);
                    intent.putExtra("text", "working like this");
                    mContext.startActivity(intent);
                }
            });

        } else {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }*/

        return convertView;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String answer=data.getStringExtra("answer");

                if(answer.equals("yes")){

                }
                else if(answer.equals("no")){

                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

}
