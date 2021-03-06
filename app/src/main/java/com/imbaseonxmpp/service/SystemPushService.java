package com.imbaseonxmpp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.imbaseonxmpp.utils.ToastUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * 系统通知推送服务
 */
public class SystemPushService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IMService.conn.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                String body = message.getBody().trim();
                if (body == null || "".equals(body)) {
                    return;
                }
                ToastUtil.showToastSafe(getApplicationContext(), body);
            }
        }, null);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
