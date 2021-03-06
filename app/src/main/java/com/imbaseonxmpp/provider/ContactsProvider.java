package com.imbaseonxmpp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.imbaseonxmpp.db.ContactsDBOpenHelper;

/**
 * 联系人提供者
 */
public class ContactsProvider extends ContentProvider {
    // 主机地址的常量:当前类的完整路径
    public static final String AUTHORITIES = ContactsProvider.class.getCanonicalName();
    // 地址匹配对象
    static UriMatcher mUriMatcher;

    // 对应联系人表的一个uri常量
    public static Uri CONTACT_URI = Uri.parse("content://" + AUTHORITIES + "/contact");

    public static final int CONTACT = 1;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 添加一个匹配的规则
        mUriMatcher.addURI(AUTHORITIES, "/contact", CONTACT);
    }

    private ContactsDBOpenHelper contactsHelper;

    /**
     * 创建帮助类对象
     */
    @Override
    public boolean onCreate() {
        contactsHelper = new ContactsDBOpenHelper(getContext());
        return true;
    }

    /**
     * 增加操作
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // 匹配后插入数据库
        switch (mUriMatcher.match(uri)) {
            case CONTACT:
                SQLiteDatabase db = contactsHelper.getWritableDatabase();
                // 新插入的id
                long id = db.insert("Contacts", "", values);
                if (id != -1) {
                    // 拼接最新的uri
                    uri = ContentUris.withAppendedId(uri, id);
                    // 通知ContentObserver数据改变了,为null就是所有都可以收到
                    getContext().getContentResolver().notifyChange(ContactsProvider.CONTACT_URI, null);
                }
                break;
        }
        return uri;
    }

    /**
     * 删除操作
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int deleteCount = 0;
        switch (mUriMatcher.match(uri)) {
            case CONTACT:
                SQLiteDatabase db = contactsHelper.getWritableDatabase();
                // 影响的行数
                deleteCount = db.delete("Contacts", selection, selectionArgs);
                if (deleteCount > 0) {
                    // 通知ContentObserver数据改变了,为null就是所有都可以收到
                    getContext().getContentResolver().notifyChange(ContactsProvider.CONTACT_URI, null);
                }
                break;
        }
        return deleteCount;
    }

    /**
     * 更新操作
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (mUriMatcher.match(uri)) {
            case CONTACT:
                SQLiteDatabase db = contactsHelper.getWritableDatabase();
                // 更新的记录总数
                updateCount = db.update("Contacts", values, selection, selectionArgs);
                if (updateCount > 0) {
                    // 通知ContentObserver数据改变了,为null就是所有都可以收到
                    getContext().getContentResolver().notifyChange(ContactsProvider.CONTACT_URI, null);
                }
                break;
        }
        return updateCount;
    }

    /**
     * 查询操作
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case CONTACT:
                SQLiteDatabase db = contactsHelper.getReadableDatabase();
                cursor = db.query("Contacts", projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
