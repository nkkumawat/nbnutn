package me.nkkumawat.chatzzz.Socket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.Connection.Connection;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Utility.Utility;

/**
 * Created by sonu on 21/1/18.
 */

public class SocketConnection {
    public static io.socket.client.Socket socket = null;


    public static void ConnectSocket(final Context context) {
        if(socket == null) {
            final DbHelper dbHelper = new DbHelper(context);
            try {
                socket = IO.socket("http://192.168.1.70:3000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            socket.on(io.socket.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("Socket", "Emmited in SocketConnection");
                    SharedPreferences prefs = context.getSharedPreferences("user", context.MODE_PRIVATE);
                    String myMobile = prefs.getString("mobile", null);
                    socket.emit("join", myMobile);
                    getAllPandingMessages(myMobile, context);
                }
            });
            socket.connect();
            socket.on("sent-by-server", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(args[0].toString());
                        final String message = jsonObject.getString("message") , sender = jsonObject.getString("sender") , receiver = jsonObject.getString("receiver");
                        Log.d("Socket New Messages", message + "E");
                        final String mediaType = jsonObject.getString("media_type");
                        if(mediaType.equals("image")) {
                            byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date();
                            String fileName = dateFormat.format(date)+ ".png";
                            Utility.saveImageToExternalStorage(decodedByte , fileName);
                            dbHelper.insertChatMessage(fileName , sender , receiver, "received" , mediaType);
                        }else {
                            dbHelper.insertChatMessage(message , sender , receiver, "received" , mediaType);
                        }
                        shoNotiFication(message , "CHATZZZ" , context);
                    } catch (JSONException e) {
                        Log.d("SocketFight=========" , e.toString() );
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    public static void getAllPandingMessages(final String mobile , final Context context)  {
        String Url = "http://192.168.1.70:3000/getallmessages";
        String Parameters = "{ mobile :"+mobile+" }";
        final DbHelper dbHelper = new DbHelper(context);
        Connection.post(Url , Parameters , true, new Connection.ConnectionResponse() {
            @Override
            public void JsonResponse(JSONObject jsonObj, boolean Success) {
                try {
                    Log.d("getAll------>" , jsonObj.toString());
                    if (!Success || jsonObj.getInt("status") != 200) {
                        return;
                    }
                    if(jsonObj.getString("status").equals("200")) {
                        JSONArray jsonArray = new JSONArray(jsonObj.getString("body"));
                        String messageToNotification = "";
                        for(int i = 0; i < jsonArray.length() ; i ++) {
                           JSONObject jsonObject = jsonArray.getJSONObject(i);
                           messageToNotification += jsonObject.getString("message") +"\n";
                           final String mediaType = jsonObject.getString("media_type");
                           if(mediaType.equals("image")) {
                                byte[] decodedString = Base64.decode(jsonObject.getString("message"), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = new Date();
                                String fileName = dateFormat.format(date)+ ".png";
                                Utility.saveImageToExternalStorage(decodedByte , fileName);
                               dbHelper.insertChatMessage(fileName , jsonObject.getString("sender") , jsonObject.getString("mobile") , "received" , jsonObject.getString("media_type"));
                           }else {
                               dbHelper.insertChatMessage(jsonObject.getString("message") , jsonObject.getString("sender") , jsonObject.getString("mobile") , "received" , jsonObject.getString("media_type"));
                           }
                        }
                        if(jsonArray.length()!= 0)
                        shoNotiFication(messageToNotification , "CHATZZZ" , context);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static void shoNotiFication(String message , String sender, Context context)  {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(sender)
                .setContentText(message)
                .setSound(soundUri);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
