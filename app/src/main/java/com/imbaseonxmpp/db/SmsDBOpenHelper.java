package com.imbaseonxmpp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @Description:
 */
public class SmsDBOpenHelper extends SQLiteOpenHelper {

    public SmsDBOpenHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Sms (_id integer primary key autoincrement,from_account text," +
                "to_account text,body text,type text,time text,session_account text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
