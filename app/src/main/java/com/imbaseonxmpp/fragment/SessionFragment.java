package com.imbaseonxmpp.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.activity.ChatActivity;
import com.imbaseonxmpp.provider.ContactsProvider;
import com.imbaseonxmpp.provider.SmsProvider;
import com.imbaseonxmpp.service.IMService;
import com.imbaseonxmpp.utils.ThreadUtil;

/**
 * 对话界面
 */
public class SessionFragment extends Fragment {
    private ListView sessionListView;
    private CursorAdapter sessionAdapter;
    private SessionContentObserver sessionContentObserver = new SessionContentObserver(new Handler());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View sessionView = inflater.inflate(R.layout.fragment_session, container, false);
        //初始化View
        initView(sessionView);
        return sessionView;
    }

    /**
     * 初始化View
     *
     * @param sessionView
     */
    private void initView(View sessionView) {
        sessionListView = (ListView) sessionView.findViewById(R.id.lv_session);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //绑定监听
        registerContentObserver();
        //初始化数据
        initData();

        //监听器
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //设置数据
        setOrUpdateAdapter();
    }

    private void setOrUpdateAdapter() {
        //判断Adapter是否存在
        if (sessionAdapter != null) {
            // 只执行刷新
            sessionAdapter.getCursor().requery();
            return;
        }
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //得到一个游标
                final Cursor cursor = getActivity().getContentResolver().query(SmsProvider.SESSION_URI, null, null,
                        new String[]{IMService.currentAccount, IMService.currentAccount}, null);
                if (cursor.getCount() <= 0) {
                    return;
                }
                //显示到联系人列表
                ThreadUtil.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置Adapter
                        sessionAdapter = new CursorAdapter(getActivity(), cursor) {
                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                View sessionView = View.inflate(context, R.layout.item_session, null);
                                return sessionView;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                TextView nicknameText = (TextView) view.findViewById(R.id.tv_session_nickname);
                                TextView bodyText = (TextView) view.findViewById(R.id.tv_session_body);

                                String sessionAccount = cursor.getString(cursor.getColumnIndex("session_account"));
                                String bodyStr = cursor.getString(cursor.getColumnIndex("body"));
                                String nicknameStr = getNickNameByAccount(sessionAccount);

                                nicknameText.setText(nicknameStr);
                                bodyText.setText(bodyStr);
                            }
                        };
                        sessionListView.setAdapter(sessionAdapter);
                    }
                });
            }
        });
    }

    /**
     * 根据账户返回别名
     */
    public String getNickNameByAccount(String account) {
        String nickName = "";
        Cursor c = getActivity().getContentResolver().query(ContactsProvider.CONTACT_URI, null, "account =?", new String[]{account}, null);
        if (c.getCount() > 0) {// 有数据
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex("nickname"));
        }
        return nickName;
    }

    /**
     * 绑定监听器
     */
    private void initListener() {
        sessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = sessionAdapter.getCursor();
                c.moveToPosition(position);
                // 拿到jid(账号)-->发送消息的时候需要
                String account = c.getString(c.getColumnIndex("session_account"));
                // 拿到nickName-->显示效果
                String nickname = getNickNameByAccount(account);

                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra("account", account);
                intent.putExtra("nickname", nickname);

                startActivity(intent);
            }
        });
    }

    /**
     * 反注册监听
     */
    @Override
    public void onDestroy() {
        unRegisterContentObserver();
        super.onDestroy();
    }

    /*=============== 监听数据库记录的改变 ===============*/

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getActivity().getContentResolver().registerContentObserver(SmsProvider.SMS_URI, true,
                sessionContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(sessionContentObserver);
    }

    /**
     * @Description:内容观察者实时监听状态改变
     */
    public class SessionContentObserver extends ContentObserver {

        public SessionContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 数据库发生改变收到通知
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            // 更新或者新建Adapter
            setOrUpdateAdapter();
        }

    }


}
