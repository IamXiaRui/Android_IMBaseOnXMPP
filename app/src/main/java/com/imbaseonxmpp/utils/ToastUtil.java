package com.imbaseonxmpp.utils;


import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 */
public class ToastUtil {
    /**
     * 可以在子线程中弹出toast
     *
     * @param context
     * @param text
     */
    public static void showToastSafe(final Context context, final String text) {
        ThreadUtil.runInMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
