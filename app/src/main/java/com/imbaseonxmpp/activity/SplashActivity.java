package com.imbaseonxmpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.utils.ThreadUtil;

/**
 * 启动引导页
 */
class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //进入登录界面
        gotoLoginActivity();
    }

    /**
     * 停留三秒进入登录界面
     */
    private void gotoLoginActivity() {
        //主线程执行
        ThreadUtil.runInChildThread(new Runnable() {
            @Override
            public void run() {
                //停留三秒
                SystemClock.sleep(3000);
                //进入登录界面
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
