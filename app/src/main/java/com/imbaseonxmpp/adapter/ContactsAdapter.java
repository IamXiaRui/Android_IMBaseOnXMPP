package com.imbaseonxmpp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.imbaseonxmpp.R;

/**
 * @Description:联系人列表适配器
 */
public class ContactsAdapter extends CursorAdapter {
    private Cursor contactCursor;

    public ContactsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.contactCursor = cursor;
    }

    /**
     * 返回一个具体的根视图
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View contactView = View.inflate(context, R.layout.item_contact, null);
        return contactView;
    }

    /**
     * 设置显示数据
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView accountText = (TextView) view.findViewById(R.id.tv_account);
        TextView nicknameText = (TextView) view.findViewById(R.id.tv_nickname);
        String accountStr = cursor.getString(contactCursor.getColumnIndex("account"));
        String nicknameStr = cursor.getString(contactCursor.getColumnIndex("nickname"));
        accountText.setText(accountStr);
        nicknameText.setText(nicknameStr);
    }
}
