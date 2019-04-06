package me.nkkumawat.chatzzz.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import me.nkkumawat.chatzzz.Connection.Connection;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.R;
import okhttp3.OkHttpClient;

public class Signup extends AppCompatActivity {
    private EditText  otp_et;
    private Button  signup_btn;
    private  String mobileNo , otp;
    private final OkHttpClient client = new OkHttpClient();
    private DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();
        Bundle bundle = getIntent().getExtras();
        dbHelper = new DbHelper(this);
        if(bundle != null) {
            mobileNo = bundle.getString("mobile");
            otp = bundle.getString("otp");
        }
        otp_et = (EditText)findViewById(R.id.otp_et);
        signup_btn = (Button)findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otp_et.getText().toString().equals(otp)) {
                    signUp(mobileNo);
                }else {
                    Toast.makeText(Signup.this , "OTP not maching" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void signUp(final String mobile)  {
        String Url = "http://192.168.1.70:3000/signup";
        String Parameters = "{ mobile :" + mobile +"}";

        Connection.post(Url , Parameters , true, new Connection.ConnectionResponse() {
            @Override
            public void JsonResponse(JSONObject jsonObj, boolean Success) {
                try {
                    if (!Success || jsonObj.getInt("status") != 200) {
                        return;
                    }
                    if(jsonObj.getString("status").equals("200")) {
                        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                        editor.putString("mobile", mobile);
                        editor.apply();
                        Intent intent = new Intent(Signup.this , ChatHome.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(Signup.this , "Somthing went wrong" , Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
