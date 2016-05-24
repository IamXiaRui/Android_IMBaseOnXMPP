package com.imbaseonxmpp.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import org.jivesoftware.smack.packet.Message;

public class ChatActivity extends AppCompatActivity {
    private String chatAccount, chatNickName;
    private ListView chatListView;
    private TextView chatTitleText;
    private Button sendButton;
    private EditText inputEText;
    private SmsAdapter smsAdapter;
    private IMService imService;

    private ChatContentObserver chatContentObserver = new ChatContentObserver(new Handler());
    private ChatServiceConnection chatServiceConnection = new ChatServiceConnection();


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
     * 初始化
     */
    private void init() {
        //注册监听
        registerContentObserver();

        //绑定服务
        Intent serviceIntent = new Intent(ChatActivity.this, IMService.class);
        bindService(serviceIntent, chatServiceConnection, BIND_AUTO_CREATE);

        //得到数据
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
     * 设置列表Adapter还是通知更新列表
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
        //开启线程查询聊天记录并显示在列表中
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //根据查询条件 和时间升序 查询对应的聊天记录
                final Cursor cursor = getContentResolver().query(SmsProvider.SMS_URI, null, "(from_account = ? and to_account=?)or(from_account = ? and to_account= ? )",
                        new String[]{IMService.currentAccount, chatAccount, chatAccount, IMService.currentAccount}, "time ASC");
                if (cursor.getCount() <= 0) {
                    return;
                }
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

                        //初始化消息属性
                        Message msg = new Message();
                        // 当前登录的用户
                        msg.setFrom(IMService.currentAccount);
                        // 需要发送给的用户
                        msg.setTo(chatAccount);
                        // 输入框里面的内容
                        msg.setBody(inputMsg);
                        // 类型就是chat
                        msg.setType(Message.Type.chat);

                        //调用服务中的发送消息方法
                        imService.sendMessage(msg);
                        //清空输入框
                        ThreadUtil.runInMainThread(new Runnable() {
                            @Override
                            public void run() {
                                inputEText.setText("");
                            }
                        });
                    }
                });
            }
        });
    }


    /**
     * 反注册监听与解绑服务
     */
    @Override
    protected void onDestroy() {
        //反注册监听
        unRegisterContentObserver();
        //解绑服务
        if (chatServiceConnection != null) {
            unbindService(chatServiceConnection);
        }

        super.onDestroy();
    }

    /**
     * 使用ContentObserver时刻监听记录的改变
     */
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


    /**
     * 定义ServiceConnection调用服务里面的方法
     */
    class ChatServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMService.IMBinder binder = (IMService.IMBinder) service;
            imService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
