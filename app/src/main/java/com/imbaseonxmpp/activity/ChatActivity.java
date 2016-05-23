package com.imbaseonxmpp.activity;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.adapter.SmsAdapter;
import com.imbaseonxmpp.provider.SmsProvider;
import com.imbaseonxmpp.service.IMService;
import com.imbaseonxmpp.utils.ThreadUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class ChatActivity extends AppCompatActivity {
    private String chatAccount, chatNickName;
    private ListView chatListView;
    private TextView chatTitleText;
    private Button sendButton;
    private EditText inputEText;
    private SmsAdapter smsAdapter;
    private IMService imService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        initView();

        initData();

        initListener();
    }


    /**
     * 初始化基本数据
     */
    private void init() {
        registerContentObserver();
        chatAccount = getIntent().getStringExtra("account");
        chatNickName = getIntent().getStringExtra("nickname");
    }

    /**
     * 初始化View
     */
    private void initView() {
        chatTitleText = (TextView) findViewById(R.id.tv_chattitle);
        sendButton = (Button) findViewById(R.id.bt_send);
        inputEText = (EditText) findViewById(R.id.et_input);
        chatListView = (ListView) findViewById(R.id.lv_chat);

        chatTitleText.setText(chatNickName);
    }


    /**
     * 初始化数据
     */
    private void initData() {
        setAdapterOrNotify();
    }

    /**
     * 设置Adapter还是通知更新
     */
    public void setAdapterOrNotify() {
        if (smsAdapter != null) {
            // 只执行刷新
            Cursor c = smsAdapter.getCursor();
            c.requery();
            // 滚动到最后一行
            chatListView.setSelection(c.getCount() - 1);
            return;
        }
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(SmsProvider.SMS_URI, null, null, null, null);
                ThreadUtil.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        smsAdapter = new SmsAdapter(ChatActivity.this, cursor);
                        chatListView.setAdapter(smsAdapter);
                        // 滚动到最后一行
                        chatListView.setSelection(smsAdapter.getCount() - 1);
                    }
                });
            }
        });
    }


    /**
     * 绑定事件监听器
     */
    private void initListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadUtil.runInChildThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String inputMsg = inputEText.getText().toString().trim();
                            //不允许发送空消息
                            if (inputMsg == null || "".equals(inputMsg)) {
                                ThreadUtil.runInMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        inputEText.setError("不能发送空消息");
                                    }
                                });
                                return;
                            }
                            //获取消息管理者
                            ChatManager chatManager = IMService.conn.getChatManager();
                            //创建消息监听者对象
                            ChatMessageListener chatMessageListener = new ChatMessageListener();
                            //创建聊天对象
                            Chat chat = chatManager.createChat(chatAccount, chatMessageListener);
                            //设置消息
                            Message msg = new Message();
                            msg.setFrom(IMService.currentAccount);// 当前登录的用户
                            msg.setTo(chatAccount);
                            msg.setBody(inputMsg);// 输入框里面的内容
                            msg.setType(Message.Type.chat);// 类型就是chat
                            //发送消息
                            chat.sendMessage(msg);

                            //发送消息 保存消息
                            saveMessage(chatAccount, msg);

                            //清空输入框
                            ThreadUtil.runInMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    inputEText.setText("");
                                }
                            });
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    /**
     * 保存消息
     */

    private void saveMessage(String sessionAccount, Message msg) {
        ContentValues values = new ContentValues();
        values.put("from_account", msg.getFrom());
        values.put("to_account", msg.getTo());
        values.put("body", msg.getBody());
        values.put("type", msg.getType().name());
        values.put("time", System.currentTimeMillis());
        values.put("session_account", sessionAccount);
        getContentResolver().insert(SmsProvider.SMS_URI, values);
    }

    @Override
    protected void onDestroy() {
        unRegisterContentObserver();
        super.onDestroy();
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
            //收到消息 保存消息
            saveMessage(participant, message);
        }
    }

    /*=============== 使用contentObserver时刻监听记录的改变 ===============*/
    private ChatContentObserver chatContentObserver = new ChatContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void registerContentObserver() {
        getContentResolver().registerContentObserver(SmsProvider.SMS_URI, true, chatContentObserver);
    }

    /**
     * 反注册监听
     */
    public void unRegisterContentObserver() {
        getContentResolver().unregisterContentObserver(chatContentObserver);
    }

    class ChatContentObserver extends ContentObserver {

        public ChatContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 接收到数据记录的改变
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // 设置adapter或者notifyadapter
            setAdapterOrNotify();
            super.onChange(selfChange, uri);
        }
    }

}
