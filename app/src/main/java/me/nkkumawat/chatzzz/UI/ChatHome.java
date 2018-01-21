package me.nkkumawat.chatzzz.UI;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nkkumawat.chatzzz.MainActivity;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.UI.FragmentContacts;

public class ChatHome extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    Socket socket;
    String myMobile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        myMobile = prefs.getString("mobile", null);

        try {
            socket = IO.socket("http://192.168.1.70:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socketsFight();
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





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentContacts.newInstance();
                case 1:
                    return FragmentContacts.newInstance();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Chat";
                case 1:
                    return "Contacts";
            }
            return null;
        }
    }
}
