package com.imbaseonxmpp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.imbaseonxmpp.R;
import com.imbaseonxmpp.adapter.MainViewPagerAdapter;
import com.imbaseonxmpp.fragment.ChatFragment;
import com.imbaseonxmpp.fragment.ContactsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView titleText;
    private ViewPager mainPager;
    private RadioButton chatRButton, contactRButton;

    private List<Fragment> fragmentList = new ArrayList<Fragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initListener();

        initData();
    }


    /**
     * 初始化View
     */
    private void initView() {
        titleText = (TextView) findViewById(R.id.tv_title);
        mainPager = (ViewPager) findViewById(R.id.vp_main);
        chatRButton = (RadioButton) findViewById(R.id.rb_chat);
        contactRButton = (RadioButton) findViewById(R.id.rb_contact);
    }

    /**
     * 绑定监听器
     */
    private void initListener() {
        chatRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换页面
                mainPager.setCurrentItem(0);
                //切换按钮样式
                changeRButtonTheme("聊天", "#ffffff", "#272636", "#60c4f5", "#ffffff");
            }
        });

        contactRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换页面
                mainPager.setCurrentItem(1);
                //切换按钮样式
                changeRButtonTheme("联系人", "#272636", "#ffffff", "#ffffff", "#60c4f5");
            }
        });

        mainPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        //切换按钮样式
                        chatRButton.setChecked(true);
                        contactRButton.setChecked(false);
                        changeRButtonTheme("聊天", "#ffffff", "#272636", "#60c4f5", "#ffffff");
                        break;
                    case 1:
                        //切换按钮样式
                        chatRButton.setChecked(false);
                        contactRButton.setChecked(true);
                        changeRButtonTheme("联系人", "#272636", "#ffffff", "#ffffff", "#60c4f5");
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 点击切换按钮样式
     *
     * @param title            标题文本
     * @param chatTextColor    聊天按钮字体颜色
     * @param contactTextColor 联系人按钮字体颜色
     * @param chatBackColor    聊天按钮背景
     * @param contactBackColor 联系人按钮背景
     */
    private void changeRButtonTheme(String title, String chatTextColor, String contactTextColor, String chatBackColor, String contactBackColor) {
        titleText.setText(title);
        chatRButton.setTextColor(Color.parseColor(chatTextColor));
        contactRButton.setTextColor(Color.parseColor(contactTextColor));
        chatRButton.setBackgroundColor(Color.parseColor(chatBackColor));
        contactRButton.setBackgroundColor(Color.parseColor(contactBackColor));
    }


    /**
     * 初始化数据
     */
    private void initData() {

        fragmentList.add(new ChatFragment());
        fragmentList.add(new ContactsFragment());

        mainPager.setAdapter(new MainViewPagerAdapter(fragmentList, getSupportFragmentManager()));
    }
}
