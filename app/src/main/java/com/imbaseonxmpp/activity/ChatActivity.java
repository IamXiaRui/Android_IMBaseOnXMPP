package com.imbaseonxmpp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.service.IMService;
import com.imbaseonxmpp.utils.ThreadUtil;
import com.imbaseonxmpp.utils.ToastUtil;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class ChatActivity extends AppCompatActivity {
    private String chatAccount, chatNickName;
    private TextView chatTitleText;
    private Button sendButton;
    private EditText inputEText;
    private IMService imService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        initView();

        initListener();
    }

    /**
     * 初始化基本数据
     */
    private void init() {
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

        chatTitleText.setText(chatNickName);
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
                            final String inputMsg = inputEText.getText().toString();
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

    class ChatMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            ToastUtil.showToastSafe(ChatActivity.this,message.getBody());
        }
    }


}
