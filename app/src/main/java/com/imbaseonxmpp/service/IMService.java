package com.imbaseonxmpp.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.imbaseonxmpp.activity.LoginActivity;
import com.imbaseonxmpp.provider.ContactsProvider;
import com.imbaseonxmpp.provider.SmsProvider;
import com.imbaseonxmpp.utils.PinyinUtil;
import com.imbaseonxmpp.utils.ThreadUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:联系人更新服务
 */
public class IMService extends Service {

    public static XMPPConnection conn;
    public static String currentAccount;
    private Roster roster;
    private ContactsRosterListener contactsRosterListener;

    private ChatManager chatManager;
    private ChatMessageListener chatMessageListener = new ChatMessageListener();
    private IMChatManagerListener imChatManagerListener = new IMChatManagerListener();
    private Chat currentChat;
    private Map<String, Chat> chatMap = new HashMap<String, Chat>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IMBinder();
    }

    public class IMBinder extends Binder {
        /**
         * 返回service的实例
         */
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public void onCreate() {

        //开启子线程同步联系人名单
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

                //获取消息管理者
                if (chatManager == null) {
                    chatManager = IMService.conn.getChatManager();
                }
                chatManager.addChatListener(imChatManagerListener);
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
        if (currentChat != null && chatMessageListener != null) {
            currentChat.removeMessageListener(chatMessageListener);
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


    /**
     * 消息监听者
     */
    class ChatMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            String msgbody = message.getBody();
            if (msgbody == null || "".equals(msgbody)) {
                return;
            }
            //得到发送者账户
            String participant = chat.getParticipant();
            // 统一格式化处理
            participant = formatAccount(participant);
            //收到消息 保存消息
            saveMessage(participant, message);
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(final Message msg) {
        try {
            String toAccount = msg.getTo();
            //创建并保存聊天对象
            if (chatMap.containsKey(toAccount)) {
                currentChat = chatMap.get(toAccount);
            } else {
                currentChat = chatManager.createChat(toAccount, chatMessageListener);
                chatMap.put(toAccount, currentChat);
            }
            //发送消息
            currentChat.sendMessage(msg);

            //发送消息 保存消息
            saveMessage(toAccount, msg);

        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存消息
     */

    private void saveMessage(String sessionAccount, Message msg) {
        ContentValues values = new ContentValues();
        //统一格式化处理
        sessionAccount = formatAccount(sessionAccount);
        String from = formatAccount(msg.getFrom());
        String to = formatAccount(msg.getTo());
        //添加数据
        values.put("from_account", from);
        values.put("to_account", to);
        values.put("body", msg.getBody());
        values.put("type", msg.getType().name());
        values.put("time", System.currentTimeMillis());
        values.put("session_account", sessionAccount);

        getContentResolver().insert(SmsProvider.SMS_URI, values);
    }

    /**
     * 账户格式化处理
     *
     * @param accout
     * @return
     */
    private String formatAccount(String accout) {
        return accout.substring(0, accout.indexOf("@")) + "@" + LoginActivity.SERVICENAME;
    }

    /**
     * 别人发送消息的监听类
     */
    class IMChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean b) {
            // 判断chat是否存在map里面
            String participant = chat.getParticipant();// 和我聊天的那个人

            // 统一格式化处理
            participant = formatAccount(participant);

            if (!chatMap.containsKey(participant)) {
                // 保存chat
                chatMap.put(participant, chat);
                //增加监听事件
                chat.addMessageListener(chatMessageListener);
            }
        }
    }
}
