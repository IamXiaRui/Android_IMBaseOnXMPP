package com.imbaseonxmpp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.service.IMService;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * @Description:消息列表适配器
 */
public class SmsAdapter extends CursorAdapter {
    private Cursor contactCursor;
    private Context context;

    private static final int RECEIVE = 0;
    private static final int SEND = 1;

    public SmsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.contactCursor = cursor;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        contactCursor.moveToPosition(position);
        String fromAccount = contactCursor.getString(contactCursor.getColumnIndex("from_account"));
        if (!IMService.currentAccount.equals(fromAccount)) {
            //接受者
            return RECEIVE;
        } else {
            //发送者
            return SEND;
        }
    }

    /**
     * 消息列表两种样式
     *
     * @return
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //是接受者
        if (getItemViewType(position) == RECEIVE) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_chat_receive, null);
            }
            viewHolder = ViewHolder.getViewHolder(convertView);

            // 得到数据,展示数据
            contactCursor.moveToPosition(position);

            String time = contactCursor.getString(contactCursor.getColumnIndex("time"));
            String body = contactCursor.getString(contactCursor.getColumnIndex("body"));

            String formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));

            viewHolder.receiveTimeText.setText(formatTime);
            viewHolder.receiveBodyText.setText(body);
        }
        //是发送者
        else if (getItemViewType(position) == SEND) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_chat_send, null);
            }

            viewHolder = ViewHolder.getViewHolder(convertView);

            // 得到数据,展示数据
            contactCursor.moveToPosition(position);

            String time = contactCursor.getString(contactCursor.getColumnIndex("time"));
            String body = contactCursor.getString(contactCursor.getColumnIndex("body"));

            String formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));

            viewHolder.sendTimeText.setText(formatTime);
            viewHolder.sendBodyText.setText(body);
        }

        return convertView;
    }

    /**
     * @ClassName: ViewHolder
     * @Description:创建一个ViewHolder
     */

    static class ViewHolder {
        TextView sendTimeText, receiveTimeText;
        TextView sendBodyText, receiveBodyText;
        ImageView sendHeadImage, receiveHeadImage;

        public ViewHolder(View convertView) {
            sendTimeText = (TextView) convertView.findViewById(R.id.tv_send_time);
            receiveTimeText = (TextView) convertView.findViewById(R.id.tv_receive_time);

            sendBodyText = (TextView) convertView.findViewById(R.id.tv_send_body);
            receiveBodyText = (TextView) convertView.findViewById(R.id.tv_receive_body);

            sendHeadImage = (ImageView) convertView.findViewById(R.id.iv_send_head);
            receiveHeadImage = (ImageView) convertView.findViewById(R.id.iv_receive_head);
        }

        public static ViewHolder getViewHolder(View convertView) {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            return viewHolder;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    /**
     * 设置显示数据
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }
}
