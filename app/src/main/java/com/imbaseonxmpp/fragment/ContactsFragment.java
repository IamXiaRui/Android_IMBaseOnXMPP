package com.imbaseonxmpp.fragment;


import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.adapter.ContactsAdapter;
import com.imbaseonxmpp.provider.ContactsProvider;
import com.imbaseonxmpp.utils.ThreadUtil;

/**
 * 联系人页面
 */
public class ContactsFragment extends Fragment {
    private ListView contactListView;

    private ContactsAdapter contactsAdapter;
    private ContactsContentObserver contactsContentObserver = new ContactsContentObserver(new Handler());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        //初始化View
        initView(contactsView);
        return contactsView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //初始化数据
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 注册监听
     */
    private void init() {
        registerContentObserver();
    }

    /**
     * 初始化View
     */
    private void initView(View view) {
        contactListView = (ListView) view.findViewById(R.id.lv_contact);
    }


    /**
     * 初始化联系人列表数据
     */
    private void initData() {
        //设置数据
        setOrUpdateAdapter();
    }

    /**
     * 根据Adapter是否存在判断是更新Adapter还是新建Adapter
     */
    public void setOrUpdateAdapter() {
        //判断Adapter是否存在
        if (contactsAdapter != null) {
            // 只执行刷新
            contactsAdapter.getCursor().requery();
            return;
        }
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //得到一个游标
                final Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.CONTACT_URI, null, null, null, null);
                if (cursor.getCount() <= 0) {
                    return;
                }
                //显示到联系人列表
                ThreadUtil.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置Adapter
                        contactsAdapter = new ContactsAdapter(getContext(), cursor);
                        contactListView.setAdapter(contactsAdapter);
                    }
                });
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
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.CONTACT_URI, true,
                contactsContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(contactsContentObserver);
    }

    /**
     * @Description:内容观察者实时监听状态改变
     */
    public class ContactsContentObserver extends ContentObserver {

        public ContactsContentObserver(Handler handler) {
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
