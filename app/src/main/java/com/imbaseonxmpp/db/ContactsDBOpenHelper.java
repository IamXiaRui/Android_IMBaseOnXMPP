package com.imbaseonxmpp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 联系人数据库帮助类
 */
public class ContactsDBOpenHelper extends SQLiteOpenHelper {

    public ContactsDBOpenHelper(Context context) {
        super(context, "contacts.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Contacts (_id integer primary key autoincrement,account text,nickname text,avatar text,pinyin text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
