package me.nkkumawat.chatzzz;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import me.nkkumawat.chatzzz.Connection.Connection;
import me.nkkumawat.chatzzz.Services.MessageCheckService;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.UI.ChatHome;
import me.nkkumawat.chatzzz.UI.Signup;

public class MainActivity extends AppCompatActivity {
    private EditText mobile_et;
    private Button sendotp_btn ;
    private String otp = "1234";
    private String mobileNo;
    private Connection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketConnection.ConnectSocket();
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String restoredText = prefs.getString("mobile", null);
        if(!isMyServiceRunning(MessageCheckService.class)) {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                if (info.isConnected()) {
                    Intent intentnew = new Intent(this, MessageCheckService.class);
                    this.startService(intentnew);
                }
            }
//            Intent intentnew = new Intent(this, MessageCheckService.class);
//            stopService(intentnew);
        }
        getContacts();
        if (restoredText != null) {
            Intent intent = new Intent(this , ChatHome.class);
            startActivity(intent);
            this.finish();
        }
        mobile_et = (EditText)findViewById(R.id.mobile_et);
        sendotp_btn = (Button)findViewById(R.id.sendotp_btn);
        sendotp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNo = mobile_et.getText().toString();
                if(mobileNo.length() == 10) {
                    sendOtp(mobileNo);
                }
            }
        });
    }
    public void sendOtp(final String mobile)  {
        String Url = "http://192.168.1.70:3000/sendotp";
        String Parameters = "{mobile:"+mobile+", otp: "+otp+"}";
        Connection.post(Url , Parameters , true, new Connection.ConnectionResponse() {
            @Override
            public void JsonResponse(JSONObject jsonObj, boolean Success) {
                try {
                    if (!Success || jsonObj.getInt("status") != 200) {
                        return;
                    }
                    if (jsonObj.get("status").equals("200")) {
                        Intent intent = new Intent(MainActivity.this, Signup.class);
                        intent.putExtra("mobile", mobile);
                        intent.putExtra("otp", otp);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Somthing went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void getContacts() {
        HashMap<String  , String> contactHash = new HashMap<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        assert phones != null;
        while (phones.moveToNext()) {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactHash.put( name , phoneNumber);
        }
        phones.close();
        JSONObject obj = new JSONObject(contactHash);
        String Url = "http://192.168.1.70:3000/matchcontact";
        String Parameters = "{  mobilenumbers : " +obj.toString() +"}";
        Connection.post(Url , Parameters , true, new Connection.ConnectionResponse() {
            @Override
            public void JsonResponse(JSONObject jsonObj, boolean Success) {
                try {
                    if (!Success || jsonObj.getInt("status") != 200) {
                        return;
                    }
                    if (jsonObj.getString("status").equals("200")) {
                        SharedPreferences.Editor editor = getSharedPreferences("contacts", MODE_PRIVATE).edit();
                        editor.putString("contacts", jsonObj.toString() );
                        editor.apply();
                        editor.commit();
                    } else {
                        Log.d("sgggggggggg" , jsonObj.toString());
//                        Toast.makeText(MainActivity.this, "Somthing went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
