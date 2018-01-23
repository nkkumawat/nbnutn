package me.nkkumawat.chatzzz.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.Connection.Connection;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.UI.ChatHome;
import me.nkkumawat.chatzzz.UI.Signup;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by sonu on 20/1/18.
 */

public class MessageCheckService extends Service {

    private final OkHttpClient client = new OkHttpClient();
    Socket socket;
    String myMobile = null;
    DbHelper dbHelper = new DbHelper(this);
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this , "service Created" , Toast.LENGTH_SHORT).show();
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);
        SocketConnection.ConnectSocket();
        socket = SocketConnection.socket;
        socketsFight();
        getAllPandingMessages(myMobile);

    }
    public void getAllPandingMessages(final String mobile)  {
        String Url = "http://192.168.1.70:3000/getallmessages";
        String Parameters = "{ mobile :"+mobile+" }";
        Connection.post(Url , Parameters , true, new Connection.ConnectionResponse() {
            @Override
            public void JsonResponse(JSONObject jsonObj, boolean Success) {
                try {
                    if (!Success || jsonObj.getInt("status") != 200) {
                        return;
                    }
                    if(jsonObj.getString("status").equals("200")) {
                        Log.d("getAll------>" , jsonObj.toString());
                        JSONArray jsonArray = new JSONArray(jsonObj.getString("body"));
                        for(int i = 0; i < jsonArray.length() ; i ++) {
                           JSONObject jsonObject = jsonArray.getJSONObject(i);
                           dbHelper.insert(jsonObject.getString("message") , jsonObject.getString("sender") , jsonObject.getString("mobile"));
                        }
                        if(jsonArray.length()!= 0)
                        shoNotiFication("New Messages" , "CHATZZZ");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
                        shoNotiFication(message , sender);
                        socket.emit("received", "{mobile:"+myMobile+"}");
                        dbHelper.insert(message , sender , receiver);
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
    }
    private void shoNotiFication(String message , String sender)  {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(sender)
                .setContentText(message)
                .setSound(soundUri);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}