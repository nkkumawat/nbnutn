package me.nkkumawat.chatzzz.UI;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;

public class Home extends AppCompatActivity {
    Socket socket;
    String myMobile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);
//
//        try {
//            socket = IO.socket("http://192.168.1.70:3000");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

//        socketsFight();
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
        socket.on("message", new Emitter.Listener() {
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





}
