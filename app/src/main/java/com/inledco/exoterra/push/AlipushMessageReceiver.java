package com.inledco.exoterra.push;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;

import java.util.Map;

public class AlipushMessageReceiver extends MessageReceiver {
    private static final String TAG = "AlipushMessageReceiver";

    @Override
    protected void onNotification(Context context, String s, String s1, Map<String, String> map) {
        Log.e(TAG, "onNotification: " + s + " " + s1 + " " + map.keySet().size());
    }

    @Override
    protected void onMessage(Context context, CPushMessage cPushMessage) {
        Log.e(TAG, "onMessage: " + cPushMessage.getAppId() + " " + cPushMessage.getMessageId() + " " + cPushMessage.getTitle());
    }

    @Override
    protected void onNotificationOpened(Context context, String s, String s1, String s2) {
        Log.e(TAG, "onNotificationOpened: " + s + " " + s1 + " " + s2);
    }

    @Override
    protected void onNotificationClickedWithNoAction(Context context, String s, String s1, String s2) {
        Log.e(TAG, "onNotificationClickedWithNoAction: " + s + " " + s1 + " " + s2);
    }

    @Override
    protected void onNotificationRemoved(Context context, String s) {
        Log.e(TAG, "onNotificationRemoved: " + s);
    }

    @Override
    protected void onNotificationReceivedInApp(Context context, String s, String s1, Map<String, String> map, int i, String s2, String s3) {
        Log.e(TAG, "onNotificationReceivedInApp: " + s + " " + s1 + " " + i + " " + s2 + " " + s3);
    }
}
