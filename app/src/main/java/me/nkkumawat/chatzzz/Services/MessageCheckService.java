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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;
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
    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this , "service Created" , Toast.LENGTH_SHORT).show();
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);
        try {
            socket = IO.socket("http://192.168.1.70:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socketsFight();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getAllPandingMessages(String mobile) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("mobile", mobile)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.70:3000/getallmessages")
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JSONObject jsonObj = new JSONObject(response.body().string());
            System.out.println(jsonObj.get("status"));
            if (jsonObj.getString("status").equals("200")) {
                SharedPreferences.Editor editor = getSharedPreferences("messages", MODE_PRIVATE).edit();
                editor.putString("mobile", mobile);
                editor.apply();

            } else {
                Toast.makeText(this, "Somthing went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void socketsFight() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
//                socket.emit("online", "{user:'nk'}");
                Log.d("Nk" , "Emmited");
//                    socket.disconnect();
            }
        });
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
        socket.connect();

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