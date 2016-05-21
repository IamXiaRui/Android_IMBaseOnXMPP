package com.imbaseonxmpp.utils;

import android.os.Handler;

/**
 * @Description:线程工具类
 */
public class ThreadUtil {

    /**
     * 子线程执行任务
     *
     * @param task 开启任务
     */
    public static void runInChildThread(Runnable task) {
        new Thread(task).start();
    }

    public static Handler handler = new Handler();

    /**
     * 主线程执行任务
     *
     * @param task 开启任务
     */
    public static void runInMainThread(Runnable task) {
        handler.post(task);
    }

}
