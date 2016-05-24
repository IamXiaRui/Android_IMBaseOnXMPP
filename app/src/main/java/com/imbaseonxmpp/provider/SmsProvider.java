package com.imbaseonxmpp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.imbaseonxmpp.db.SmsDBOpenHelper;

/**
 * 消息内容提供者
 */
public class SmsProvider extends ContentProvider {
    // 主机地址的常量:当前类的完整路径
    private static final String AUTHORITIES = SmsProvider.class.getCanonicalName();
    // 地址匹配对象
    static UriMatcher mUriMatcher;
    // 对应消息表的uri常量
    public static Uri SESSION_URI = Uri.parse("content://" + AUTHORITIES + "/session");
    public static Uri SMS_URI = Uri.parse("content://" + AUTHORITIES + "/sms");

    public static final int SMS = 1;
    public static final int SESSION = 2;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 添加匹配规则
        mUriMatcher.addURI(AUTHORITIES, "/sms", SMS);
        mUriMatcher.addURI(AUTHORITIES, "/session", SESSION);
    }

    private SmsDBOpenHelper smsDBOpenHelper;

    @Override
    public boolean onCreate() {
        smsDBOpenHelper = new SmsDBOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /* =========== CRUD Begin =========== */

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case SMS:
                // 插入之后对于的id
                long id = smsDBOpenHelper.getWritableDatabase().insert("Sms", "", values);
                if (id > 0) {
                    uri = ContentUris.withAppendedId(uri, id);
                    // 发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.SMS_URI, null);
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                // 具体删除了几条数据
                deleteCount = smsDBOpenHelper.getWritableDatabase().delete("Sms", selection, selectionArgs);
                if (deleteCount > 0) {
                    // 发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.SMS_URI, null);
                }
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                // 更新了几条数据
                updateCount = smsDBOpenHelper.getWritableDatabase().update("Sms", values, selection, selectionArgs);
                if (updateCount > 0) {
                    // 发送数据改变的信号
                    getContext().getContentResolver().notifyChange(SmsProvider.SMS_URI, null);
                }
                break;
        }
        return updateCount;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case SMS:
                cursor = smsDBOpenHelper.getReadableDatabase().query("Sms", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SESSION:
                cursor = smsDBOpenHelper.getReadableDatabase().rawQuery("SELECT * FROM " +
                        "(SELECT * FROM Sms WHERE from_account = ? or to_account = ? ORDER BY time ASC)" +
                        "GROUP BY session_account", selectionArgs);
        }
        return cursor;
    }

    /* =========== CRUD End =========== */
}
