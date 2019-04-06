package me.nkkumawat.chatzzz.Utility;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.Services.MessageCheckService;
import me.nkkumawat.chatzzz.Socket.SocketConnection;

/**
 * Created by sonu on 20/1/18.
 */

public class InternetCheck  extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context , "started" , Toast.LENGTH_LONG).show();
        if(Utility.isInternetConnected(context)) {
            if(Utility.isMyServiceRunning(MessageCheckService.class, context)) {
                Intent intentnew = new Intent(context, MessageCheckService.class);
                context.startService(intentnew);
            }
            DbHelper dbHelper = new DbHelper(context);
            Cursor cursor = dbHelper.getAllPandingData();
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                String message = cursor.getString(1);
                String myMobile = cursor.getString(2);
                String SingleChatMobile = cursor.getColumnName(3);
                String media_type = cursor.getString(6);
                String jsonString = "{message:'" + message + "' , sender: " + myMobile + " , receiver : " + SingleChatMobile + " , media_type : "+media_type+"}";
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SocketConnection.socket.emit("sent-by-device", jsonObject);
                cursor.moveToNext();
            }
        }
    }
}