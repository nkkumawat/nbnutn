package me.nkkumawat.chatzzz.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by sonu on 21/1/18.
 */

public class DbHelper  extends SQLiteOpenHelper {

    public static final String CONTENT_AUTHORITY = "me.nkkumawat.sockets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CHAT_SMS = "chatSms";
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHAT_SMS).build();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chatSms";
    public static final String CHAT_SMS = "chatSms";
    String notFound = "notFound";
    // Shops Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_USER_FROM = "user_from";
    public static final String KEY_USER_TO = "user_to";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + CHAT_SMS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE + " TEXT,"+ KEY_USER_FROM + " TEXT," + KEY_USER_TO + " TEXT" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_SMS);
        onCreate(db);
    }
    public void insert(String message  , String user_from , String user_to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, message);
        values.put(KEY_USER_FROM, user_from);
        values.put(KEY_USER_TO, user_to);
        db.insert(CHAT_SMS, null, values);
        db.close();
    }
    public Cursor getWholeData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO};
        Cursor cursor = db.query(CHAT_SMS,columns, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return  cursor;
    }
    public Cursor getMobileWiseData(String FROM) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{KEY_ID, KEY_MESSAGE, KEY_USER_FROM , KEY_USER_TO};
        Cursor cursor = db.query(CHAT_SMS,columns, KEY_USER_FROM + "=? or " + KEY_USER_TO + "=?", new String[]{FROM ,FROM }, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return  cursor;
    }
    //    public String getDataPass(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(WAY_2, new String[]{KEY_ID, KEY_MOB, KEY_PASS}, KEY_ID + "=?",  new String[]{String.valueOf(id)}, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
//        else
//            return notFound;
//        return  cursor.getString(2);
//    }
    public int getDatabaseSize() {
        String countQuery = "SELECT  * FROM " + CHAT_SMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

}