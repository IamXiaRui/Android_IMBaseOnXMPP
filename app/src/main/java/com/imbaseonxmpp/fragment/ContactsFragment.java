package com.imbaseonxmpp.fragment;


import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.adapter.ContactsAdapter;
import com.imbaseonxmpp.provider.ContactsProvider;
import com.imbaseonxmpp.service.IMService;
import com.imbaseonxmpp.utils.PinyinUtil;
import com.imbaseonxmpp.utils.ThreadUtil;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import java.util.Collection;

/**
 * 联系人页面
 */
public class ContactsFragment extends Fragment {
    private ListView contactListView;

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

        //绑定监听器
        initListener();

        //初始化数据
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {
    }

    private void initView(View view) {
        contactListView = (ListView) view.findViewById(R.id.lv_contact);
    }

    private void initListener() {
    }

    /**
     * 初始化联系人列表数据
     */
    private void initData() {

        //开启子线程获取数据
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //得到联系人名单对象
                Roster roster = IMService.conn.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();
                //循环遍历名单
                for (RosterEntry entry : entries) {
                    //保存或者更新联系人名单
                    saveOrUpdateEntry(entry);
                }
                //得到一个游标
                final Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.CONTACT_URI, null, null, null, null);
                //显示到联系人列表
                ThreadUtil.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置Adapter
                        contactListView.setAdapter(new ContactsAdapter(getContext(), cursor));
                    }
                });
            }
        });
    }

    /**
     * 保存或者更新联系人名单
     *
     * @param entry
     */
    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        String account = entry.getUser();
        values.put("account", account);

        String nickname = entry.getName();
        //昵称判空处理
        if (nickname == null || "".equals(nickname)) {
            //将账户@前的字符赋值给昵称
            nickname = account.substring(0, account.indexOf("@"));
        }
        values.put("nickname", nickname);
        values.put("avatar", "");
        values.put("pinyin", PinyinUtil.getPinyin(account));

        //先更新后插入
        int updateAccount = getActivity().getContentResolver().update(ContactsProvider.CONTACT_URI, values, "account=?", new String[]{account});
        if (updateAccount <= 0) {
            getActivity().getContentResolver().insert(ContactsProvider.CONTACT_URI, values);
        }
    }

}
