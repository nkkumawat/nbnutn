package me.nkkumawat.chatzzz.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

import me.nkkumawat.chatzzz.Model.Contacts;
import me.nkkumawat.chatzzz.Model.NewChatNumber;
import me.nkkumawat.chatzzz.Utility.DataDeleteListener;
import me.nkkumawat.chatzzz.Utility.DatabaseChangedReceiver;

/**
 * Created by sonu on 21/1/18.
 */

public class DbHelper  extends SQLiteOpenHelper {

    public static final String CONTENT_AUTHORITY = "me.nkkumawat.sockets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CHAT_SMS = "chatSms";
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHAT_SMS).build();

    public static final int DATABASE_VERSION = 30;
    public static final String DATABASE_NAME = "chatSms";
    public static final String CHAT_SMS = "chatSms";
    public static final String MY_CONTACTS = "myContacts";
    public static final String PANDING_MSGS = "paningMsgs";

    String notFound = "notFound";
    // Shops Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_USER_FROM = "user_from";
    public static final String KEY_USER_TO = "user_to";
    public static final String KEY_MSG_TYPE = "msg_type";
    public static final String KEY_TIME_STAMP = "msg_time";
    public static final String KEY_MEDIA_TYPE = "media_type";
    public static final String KEY_PIC_URL = "picture_url";

//  ==================

    public static final String KEY_NAME = "name";
    public static final String KEY_MOBILE_NO = "mobile_no";



    public Context context;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CHAT_TABLE = "CREATE TABLE " + CHAT_SMS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE + " TEXT,"+ KEY_USER_FROM + " TEXT," + KEY_USER_TO + " TEXT,"+ KEY_MSG_TYPE + " TEXT ," +KEY_TIME_STAMP+" DATETIME DEFAULT CURRENT_TIMESTAMP , "+KEY_MEDIA_TYPE+" TEXT" +")";
        String CREATE_PANDING_MSGS_TABLE = "CREATE TABLE " + PANDING_MSGS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE + " TEXT,"+ KEY_USER_FROM + " TEXT," + KEY_USER_TO + " TEXT,"+ KEY_MSG_TYPE + " TEXT ," +KEY_TIME_STAMP+" DATETIME DEFAULT CURRENT_TIMESTAMP , "+KEY_MEDIA_TYPE+" TEXT" +")";
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + MY_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"+ KEY_MOBILE_NO + " TEXT , " + KEY_PIC_URL + " TEXT"+")";

        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_PANDING_MSGS_TABLE);
        db.execSQL(CREATE_CHAT_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_SMS);
        db.execSQL("DROP TABLE IF EXISTS " + MY_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + PANDING_MSGS);
        onCreate(db);
    }
    public void insertChatMessage(String message  , String user_from , String user_to, String msg_type , String media_type) {
        Log.d("----------------------" , message);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, message);
        values.put(KEY_USER_FROM, user_from);
        values.put(KEY_USER_TO, user_to);
        values.put(KEY_MSG_TYPE, msg_type);
        values.put(KEY_MEDIA_TYPE, media_type);
        db.insert(CHAT_SMS, null, values);
        db.close();
        NewChatNumber.mobileNo1 = user_from;
        NewChatNumber.mobileNo2 = user_to;
        this.context.sendBroadcast(new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
    }
    public void insertPandingChatMessage(String message  , String user_from , String user_to, String msg_type , String media_type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, message);
        values.put(KEY_USER_FROM, user_from);
        values.put(KEY_USER_TO, user_to);
        values.put(KEY_MSG_TYPE, msg_type);
        values.put(KEY_MEDIA_TYPE, media_type);
        db.insert(PANDING_MSGS, null, values);
        db.close();
        NewChatNumber.mobileNo1 = user_from;
        NewChatNumber.mobileNo2 = user_to;

//        this.context.sendBroadcast(new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
    }

    public void updatePictureUrl(String mobile , String pictureUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PIC_URL, pictureUrl);
        db.update(MY_CONTACTS, values, KEY_MOBILE_NO + "="+mobile, null);
        db.close();
    }

    public void insertContacts(HashMap<String  , String> contactHash ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (String name : contactHash.keySet()) {
            values.put(KEY_NAME, name);
            values.put(KEY_PIC_URL, "pic");
            values.put(KEY_MOBILE_NO, contactHash.get(name));
            db.insert(MY_CONTACTS, null, values);
        }

        db.close();
//        this.context.sendBroadcast(new Intent(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
    }
    public Cursor getAllPandingData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO, KEY_TIME_STAMP};
        Cursor cursor = db.query(CHAT_SMS,columns, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return  cursor;
    }
    public Cursor deletePandingData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO, KEY_TIME_STAMP};
        Cursor cursor = db.query(CHAT_SMS,columns, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return  cursor;
    }

    public Contacts getNameOfContactNo(String mobileNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_NAME, KEY_PIC_URL};
        Cursor cursor = db.query(MY_CONTACTS,columns, KEY_MOBILE_NO + "=? ", new String[]{mobileNo }, null, null, null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToLast();
            return new Contacts(cursor.getString(0),mobileNo , cursor.getString(1));
        }
        return new Contacts(mobileNo , mobileNo, "https://png.pngtree.com/svg/20170602/0db185fb9c.png");
    }

    public Cursor getChatMobileNumbers(String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        String col = "";
        if(column.equals("from")){
            col = KEY_USER_FROM;
        }else {
            col = KEY_USER_TO;
        }
        String[] columns = new String[]{col };
        Cursor cursor = db.query(true , CHAT_SMS,columns, null, null, col, null , null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

 	public Cursor getLastUpdatedChats(String FROM, String MY_NUMBER) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO, KEY_MSG_TYPE , KEY_TIME_STAMP , "media_type"};
        Cursor cursor = db.query(CHAT_SMS,columns, KEY_USER_FROM + "=? or " + KEY_USER_TO + "=?", new String[]{FROM ,FROM }, null, null, null);
        if (cursor != null)
            cursor.moveToLast();
        return  cursor;
    }
    public Cursor getMobileWiseChats(String FROM, String MY_NUMBER) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO, KEY_MSG_TYPE, KEY_TIME_STAMP , "media_type"};
        Cursor cursor = db.query(CHAT_SMS,columns, " ( "+KEY_USER_FROM + "=? and " + KEY_USER_TO + "=? ) or ("+KEY_USER_FROM + "=? and " + KEY_USER_TO + "=? )", new String[]{FROM ,MY_NUMBER, MY_NUMBER, FROM }, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return  cursor;
    }

    public int getDatabaseSize() {
        String countQuery = "SELECT  * FROM " + CHAT_SMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }
    public int getNumbersCount() {
        String countQuery = "SELECT  * FROM " + MY_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public void deleteMyChatToSpecific(String mobileNo) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(CHAT_SMS, KEY_USER_FROM + "=" + mobileNo + " or " + KEY_USER_TO +"=" + mobileNo, null);
        this.context.sendBroadcast(new Intent(DataDeleteListener.ACTION_DATABASE_CHANGED));
    }

}
