package me.nkkumawat.chatzzz.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import me.nkkumawat.chatzzz.Adapters.ChatArrayAdapter;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.Model.ChatMessage;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.Utility.DataDeleteListener;
import me.nkkumawat.chatzzz.Utility.DatabaseChangedReceiver;
import me.nkkumawat.chatzzz.Utility.ImageFilePath;
import me.nkkumawat.chatzzz.Utility.Utility;

public class SingleChatWindow extends AppCompatActivity {
    private int i = 0;
    private TextView msg;
    private ListView lv_chat;
    private Button send ;
    private ScrollView scroll_view;
    private Cursor cursor;
    private Socket socket;
    private DbHelper dbHelper = new DbHelper(this);
    private String myMobile = null;
    private String SingleChatMobile = "";
    private String SingleChatMobileName = "";
    private ImageButton pickImage;

    private ChatArrayAdapter chatArrayAdapter;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_chat_window);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            SingleChatMobile = bundle.getString("singlechat");
            SingleChatMobileName = dbHelper.getNameOfContactNo(SingleChatMobile).name;
        }
        getSupportActionBar().setTitle(SingleChatMobileName);
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);

        msg  = (TextView)findViewById(R.id.msg);
        lv_chat = (ListView)findViewById(R.id.lv_chat);
        pickImage = (ImageButton) findViewById(R.id.pick_image);
        send = (Button) findViewById(R.id.send);
        socket = SocketConnection.socket;
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msg.getText().toString();
                if(!message.equals("")) {
                    msg.setText("");
                    sendMessage(message, "text" , "nofile");
                }
            }
        });

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
        setTextToList();
        registerReceiver(mReceiver, new IntentFilter(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
        registerReceiver(mDeleteReceiver, new IntentFilter(DataDeleteListener.ACTION_DATABASE_CHANGED));
    }

    public void sendMessage(String message , String media_type, String fileName){
        String msgOrFile = "";
        if(media_type.equals("image")) {
            msgOrFile = fileName;
        }else {
            msgOrFile = message;
        }
        dbHelper.insertChatMessage(msgOrFile, myMobile, SingleChatMobile, "sent", media_type);
        String jsonString = "{message:'" + message + "' , sender: " + myMobile + " , receiver : " + SingleChatMobile + " , media_type : "+media_type+"}";
        Log.d("---------------" , jsonString);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(Utility.isInternetConnected(this)){
            socket.emit("sent-by-device", jsonObject);
        }else {
            dbHelper.insertPandingChatMessage(msgOrFile, myMobile, SingleChatMobile, "sent", media_type);
        }
    }
    public String getTimeOfMessage(String timestamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        try {
            return timeformat.format(dateFormat.parse(timestamp));
        }catch (Exception e) {
            return "00:00";
        }
    }
    public void setTextToList() {
        cursor = dbHelper.getMobileWiseChats(SingleChatMobile, myMobile);
        while (!cursor.isAfterLast()) {
            chatMessageList.add(new ChatMessage(cursor.getString(4) , cursor.getString(1),getTimeOfMessage(cursor.getString(5)) , cursor.getString(6)));
            cursor.moveToNext();
        }
        cursor.close();
        chatArrayAdapter = new ChatArrayAdapter(this, R.layout.list_chat , chatMessageList);
        lv_chat.setAdapter(chatArrayAdapter);
        lv_chat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatArrayAdapter.notifyDataSetChanged();
    }
    private final DatabaseChangedReceiver mReceiver = new DatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            cursor = dbHelper.getLastUpdatedChats(SingleChatMobile, myMobile);
            chatArrayAdapter.add(new ChatMessage(cursor.getString(4) , cursor.getString(1),getTimeOfMessage(cursor.getString(5)), cursor.getString(6)));
            lv_chat.setSelection(chatArrayAdapter.getCount() - 1);
            cursor.close();
        }
    };

    private final DataDeleteListener mDeleteReceiver = new DataDeleteListener() {
        public void onReceive(Context context, Intent intent) {
            chatMessageList.clear();
            setTextToList();
        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String realPath = ImageFilePath.getPath(this, data.getData());
            Log.d("Picture Path", realPath);
            Bitmap imageBitmap ;
            try {
                String encodedImage = Utility.encodeImage(realPath , 10);
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String fileName = dateFormat.format(date) + ".jpg";
                Utility.saveImageToExternalStorage(decodedByte , fileName);
                sendMessage(encodedImage, "image" , fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_chat, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.deleteall:
            dbHelper.deleteMyChatToSpecific(SingleChatMobile);
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

//    @Override
//    public void onBackPressed(){
////        finish();
//    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mDeleteReceiver);
    }
}
