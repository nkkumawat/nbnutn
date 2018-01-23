package me.nkkumawat.chatzzz.UI;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.Adapters.ChatAdapter;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.Utility.Utility;

public class SingleChatWindow extends AppCompatActivity {

    private int i = 0;
    private TextView msg;
    private ListView lv_chat;
    private Button send ;
    private ScrollView scroll_view;
    private Cursor cursor;
    private ChatAdapter adapter;
    private Socket socket;
    private DbHelper dbHelper = new DbHelper(this);
    private String myMobile = null;
    private String SingleChatMobile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat_window);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            SingleChatMobile = bundle.getString("singlechat");
        }
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);

        msg  = (TextView)findViewById(R.id.msg);
        lv_chat = (ListView)findViewById(R.id.lv_chat);
        send = (Button) findViewById(R.id.send);
        scroll_view = (ScrollView)findViewById(R.id.scroll_view);
        socket = SocketConnection.socket;
        scroll_view.post(new Runnable() {
            @Override
            public void run() {
                scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msg.getText().toString();
                msg.setText("");
                dbHelper.insert(message , myMobile , SingleChatMobile);
                String jsonString = "{message:'"+message+"' , sender: "+ myMobile+" , receiver : "+SingleChatMobile+"}";
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("receive-message",jsonObject);
                setTextToList();
            }
        });
        socketsFight();
        setTextToList();
    }
    public void socketsFight() {
        socket.on("send-message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonObject = null;
                String message = null , sender = null , receiver = null;
                try {
                    jsonObject = new JSONObject(args[0].toString());
                    message = jsonObject.getString("message");
                    sender = jsonObject.getString("sender");
                    receiver = jsonObject.getString("receiver");
                    if(receiver.equals(myMobile)){
//                        socket.emit("received", "{mobile:"+myMobile+"}");
//                        dbHelper.insert(message , sender , receiver);
                        final String finalMessage = message;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTextToList();
                            }
                        });
                    }else {
                        Log.d("NARENDRA KUMAWAT" , "NOT YOU!!!!!!!!!!!!!!!");
                    }
                } catch (JSONException e) {
                    Log.d("NARENDRA KUMAWAT" , e.toString());
                    e.printStackTrace();
                }
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {}
        });
        socket.connect();
    }
    public void setTextToList() {
//        cursor = dbHelper.getWholeData();
        cursor = dbHelper.getMobileWiseData(SingleChatMobile);
        adapter = new ChatAdapter(this, cursor);
        lv_chat.setAdapter(adapter);
        Utility.setListViewHeightBasedOnItems(lv_chat);
        scroll_view.post(new Runnable() {

            @Override
            public void run() {
                scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
//        lv_chat.setClickable(false);
//        lv_chat.setSelectionFromTop(0,
//                cursor.getColumnCount()-1 );

//        Toast.makeText(MainActivity.this , dbHelper.getDatabaseSize() + "sk" , Toast.LENGTH_LONG).show();
    }
}
