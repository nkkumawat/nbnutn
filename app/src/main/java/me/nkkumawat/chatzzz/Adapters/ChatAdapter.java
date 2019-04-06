package me.nkkumawat.chatzzz.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import me.nkkumawat.chatzzz.R;

/**
 * Created by sonu on 21/1/18.
 */

public class ChatAdapter extends CursorAdapter {

    private String myNumber;
    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        myNumber = prefs.getString("mobile", null);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if(cursor != null && cursor.getString(4).equals("received")) {
            return LayoutInflater.from(context).inflate(R.layout.list_chat, parent, false);
        }else {
            return LayoutInflater.from(context).inflate(R.layout.list_chat_right, parent, false);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(cursor != null) {
            TextView tv_message = (TextView) view.findViewById(R.id.chat_message);
            String body = cursor.getString(1);
            tv_message.setText(body);
        }
    }

    @Override
    public int getViewTypeCount() {
        return super.getCount() < 1 ? 1 : super.getCount() ;
    }


    @Override
    public int getItemViewType(int position) {
        if(getCount() == 0 || getCount() - position == 0) {
            return 0;
        }

        return getCount() - position ;
    }


}