package com.imbaseonxmpp.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.imbaseonxmpp.provider.ContactsProvider;
import com.imbaseonxmpp.utils.PinyinUtil;
import com.imbaseonxmpp.utils.ThreadUtil;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * @Description:联系人更新服务
 */
public class IMService extends Service {

    public static XMPPConnection conn;
    private Roster roster;
    private ContactsRosterListener contactsRosterListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //开启子线程获取数据
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //得到联系人名单对象
                roster = IMService.conn.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();

                //增加监听
                contactsRosterListener = new ContactsRosterListener();
                roster.addRosterListener(contactsRosterListener);

                //循环遍历名单
                for (RosterEntry entry : entries) {
                    //保存或者更新联系人名单
                    saveOrUpdateEntry(entry);
                }
            }
        });
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // 移除rosterListener
        if (roster != null && contactsRosterListener != null) {
            roster.removeRosterListener(contactsRosterListener);
        }
        super.onDestroy();
    }

    /**
     * 保存或者更新联系人名单
     *
     * @param entry
     */
    public void saveOrUpdateEntry(RosterEntry entry) {
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
        int updateAccount = getContentResolver().update(ContactsProvider.CONTACT_URI, values, "account=?", new String[]{account});
        if (updateAccount <= 0) {
            getContentResolver().insert(ContactsProvider.CONTACT_URI, values);
        }
    }

    /**
     * @Description:联系人列表监听器
     */
    class ContactsRosterListener implements RosterListener {

        /**
         * 添加操作监听
         *
         * @param addresses
         */
        @Override
        public void entriesAdded(Collection<String> addresses) {
            for (String address : addresses) {
                RosterEntry entry = roster.getEntry(address);
                saveOrUpdateEntry(entry);
            }
        }

        /**
         * 更新操作监听
         *
         * @param addresses
         */
        @Override
        public void entriesUpdated(Collection<String> addresses) {
            for (String address : addresses) {
                RosterEntry entry = roster.getEntry(address);
                saveOrUpdateEntry(entry);
            }
        }

        /**
         * 删除操作监听
         *
         * @param addresses
         */
        @Override
        public void entriesDeleted(Collection<String> addresses) {
            for (String address : addresses) {
                getContentResolver().delete(ContactsProvider.CONTACT_URI, "account=?", new String[]{address});
            }
        }

        @Override
        public void presenceChanged(Presence presence) {

        }
    }
}
