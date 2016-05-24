package com.imbaseonxmpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.service.IMService;
import com.imbaseonxmpp.service.SystemPushService;
import com.imbaseonxmpp.utils.ThreadUtil;
import com.imbaseonxmpp.utils.ToastUtil;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class LoginActivity extends Activity {

    private EditText nameEText, pwdEText;
    private Button loginButton;

    // 主机IP
    public static final String HOST = "172.25.10.172";
    // 对应的端口号
    public static final int PORT = 5222;
    //对应的主机名称
    public static final String SERVICENAME = "pc.xiarui";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //初始化View
        initView();

        //绑定监听器
        initListener();
    }

    /**
     * 初始化View
     */
    private void initView() {
        nameEText = (EditText) findViewById(R.id.et_name);
        pwdEText = (EditText) findViewById(R.id.et_pwd);
        loginButton = (Button) findViewById(R.id.bt_login);

        /*----------测试用例------------*/
        nameEText.setText("admin");
        pwdEText.setText("123456");
    }

    /**
     * 绑定监听器
     */
    private void initListener() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取出用户名和密码
                final String username = nameEText.getText().toString().trim();
                final String password = pwdEText.getText().toString().trim();
                //判断用户名与密码是否为空
                if (TextUtils.isEmpty(username)) {
                    nameEText.setError("用户名不能为空");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    pwdEText.setError("密码不能为空");
                    return;
                }
                ThreadUtil.runInChildThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 1.创建连接配置对象
                            ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);

                            // 额外的配置(方面我们开发,上线的时候,可以改回来)
                            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);// 明文传输
                            config.setDebuggerEnabled(true);// 开启调试模式,方便我们查看具体发送的内容

                            // 2.开始创建连接对象
                            XMPPConnection conn = new XMPPConnection(config);
                            // 开始连接
                            conn.connect();
                            // 开始登录
                            conn.login(username, password);
                            // 登录成功
                            ToastUtil.showToastSafe(LoginActivity.this, "登录成功");
                            finish();
                            // 跳到主界面
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            //保存连接对象
                            IMService.conn = conn;
                            // 保存当前登录的账户
                            String account = username + "@" + LoginActivity.SERVICENAME;
                            IMService.currentAccount = account;

                            //启动后台监听服务
                            Intent chatService = new Intent(LoginActivity.this, IMService.class);
                            startService(chatService);

                            //启动系统推送服务
                            Intent pushService = new Intent(LoginActivity.this, SystemPushService.class);
                            startService(pushService);

                        } catch (XMPPException e) {
                            e.printStackTrace();
                            ToastUtil.showToastSafe(LoginActivity.this, "登录失败");
                        }
                    }
                });
            }
        });
    }
}
