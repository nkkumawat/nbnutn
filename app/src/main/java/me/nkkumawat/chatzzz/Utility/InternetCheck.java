package me.nkkumawat.chatzzz.Utility;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.nkkumawat.chatzzz.Services.MessageCheckService;

/**
 * Created by sonu on 20/1/18.
 */

public class InternetCheck  extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                Intent intentnew = new Intent(context, MessageCheckService.class);
                context.startService(intentnew);
            }
        }
    }
}