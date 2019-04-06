package me.nkkumawat.chatzzz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vatsal.imagezoomer.ZoomAnimation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nkkumawat.chatzzz.Model.ChatMessage;
import me.nkkumawat.chatzzz.R;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView timeText;
    private ImageButton imgMsg;

    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId, List<ChatMessage> chatMessageList) {
        super(context, textViewResourceId);
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert chatMessageObj != null;
        if(chatMessageObj.media_type  != null && chatMessageObj.media_type.equals("image")) {
            if (!chatMessageObj.message_type.equals("received")) {
                row = inflater.inflate(R.layout.list_image_right, parent, false);
            } else {
                row = inflater.inflate(R.layout.list_image, parent, false);
            }
            imgMsg = (ImageButton) row.findViewById(R.id.image_msg);
            timeText = (TextView) row.findViewById(R.id.chat_message_time);
            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Chatzz/media/";
            File imgFile = new File(fullPath+chatMessageObj.message);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(fullPath+chatMessageObj.message);
                imgMsg.setImageBitmap(myBitmap);
            }
            timeText.setText(chatMessageObj.message_time);
            imgMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = (Activity) context;
                    ZoomAnimation zoomAnimation = new ZoomAnimation(activity);
                    zoomAnimation.zoom(v, 200);
                }
            });
        }else {
            if (!chatMessageObj.message_type.equals("received")) {
                row = inflater.inflate(R.layout.list_chat_right, parent, false);
            } else {
                row = inflater.inflate(R.layout.list_chat, parent, false);
            }
            chatText = (TextView) row.findViewById(R.id.chat_message);
            timeText = (TextView) row.findViewById(R.id.chat_message_time);
            chatText.setText(chatMessageObj.message);
            timeText.setText(chatMessageObj.message_time);
        }
        return row;
    }
}