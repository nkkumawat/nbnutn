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
import android.widget.Toast;

import io.socket.client.Socket;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import okhttp3.OkHttpClient;

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
        SocketConnection.ConnectSocket(this);
        socket = SocketConnection.socket;

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