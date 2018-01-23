package me.nkkumawat.chatzzz.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.nkkumawat.chatzzz.R;

/**
 * Created by sonu on 21/1/18.
 */

public class ChatAdapter extends CursorAdapter {

    public static Cursor c;
    public String myNumber;
    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.c = cursor;
        SharedPreferences prefs = context.getSharedPreferences("user", context.MODE_PRIVATE);
        myNumber = prefs.getString("mobile", null);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_chat, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv_message = (TextView) view.findViewById(R.id.chat_message);
        TextView tv_message_from = (TextView) view.findViewById(R.id.message_from);
        LinearLayout cardView = (LinearLayout) view.findViewById(R.id.card_view);
//        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.relative_layout);
        String body = cursor.getString(1);
        if(cursor.getString(2).equals(myNumber)) {
            tv_message_from.setText(cursor.getString(2));
            tv_message.setGravity(Gravity.RIGHT);
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.RIGHT_OF);
//            relativeLayout.setLayoutParams(params);
//            relativeLayout.setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.RIGHT;
            cardView.setLayoutParams(params);
//            cardView.setVerticalGravity(Gravity.RIGHT);
        }else {
            tv_message_from.setText(cursor.getString(2));
        }
        tv_message.setText(body);
    }

    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}