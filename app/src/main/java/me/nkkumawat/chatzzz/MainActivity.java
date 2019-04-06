package me.nkkumawat.chatzzz;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import me.nkkumawat.chatzzz.Connection.Connection;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.Services.MessageCheckService;
import me.nkkumawat.chatzzz.Socket.SocketConnection;
import me.nkkumawat.chatzzz.UI.ChatHome;
import me.nkkumawat.chatzzz.UI.Signup;
import me.nkkumawat.chatzzz.Utility.Utility;

public class MainActivity extends AppCompatActivity {
    private EditText mobile_et;
    private Button sendotp_btn ;
    private String otp = "1234";
    private String mobileNo;
    private final int REQUEST_CODE_CONTACTS = 1;
    private final int REQUEST_CODE_STORAGE = 2;
    private DbHelper dbHelper = new DbHelper(this);
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        checkForPermission();
        SocketConnection.ConnectSocket(this);
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        String restoredText = prefs.getString("mobile", null);
            if(!Utility.isMyServiceRunning(MessageCheckService.class, this)) {
                Intent intentnew = new Intent(this, MessageCheckService.class);
                this.startService(intentnew);
            }
        if(dbHelper.getNumbersCount() == 0 && checkReadContactPermission()) {
            getContacts();
        }
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
    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS ,Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CODE_CONTACTS);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permission" , "granted--------------------");
                   getContacts();
                } else {
                }
                return;
            }
        }
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
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public void getContacts() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading..."); // Setting Message
        progressDialog.setTitle("ProgressDialog"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        final HashMap<String  , String> contactHash = new HashMap<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        assert phones != null;

        while (phones.moveToNext()) {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactHash.put( name , phoneNumber);
        }
        phones.close();
        new Thread(new Runnable() {
            public void run() {
                dbHelper.insertContacts(contactHash);
            }
        }).start();
        progressDialog.dismiss();
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

                        JSONArray jsonArray = new JSONArray(jsonObj.getString("body"));
                        for (int x = 0; x < jsonArray.length(); x++) {
                            JSONObject object = jsonArray.getJSONObject(x);
                            dbHelper.updatePictureUrl(object.getString("mobile") , object.getString("picture_url"));
                        }
//
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
    private boolean checkReadContactPermission() {
        String permission = Manifest.permission.READ_CONTACTS;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
