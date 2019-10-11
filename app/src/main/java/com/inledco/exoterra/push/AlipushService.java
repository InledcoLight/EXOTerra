package com.inledco.exoterra.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alibaba.sdk.android.push.AliyunMessageIntentService;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.inledco.exoterra.R;
import com.inledco.exoterra.main.MainActivity;

import java.util.Map;

public class AlipushService extends AliyunMessageIntentService {
    private static final String TAG = "AlipushService";

    private NotificationManager mNotificationManager;

    @Override
    protected void onNotification(Context context, String s, String s1, Map<String, String> map) {
        Log.e(TAG, "onNotification: " + s + " " + s1 + " " + map.keySet().size());
        for (String key : map.keySet()) {
            Log.e(TAG, "onNotification: " + key + " - " + map.get(key));
        }

        sendNotification(s, s1);
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

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private void sendNotification(@NonNull final String title, final String content) {
        if (getNotificationManager() != null) {
            getNotificationManager().notify(0, getNotification(title, content));
        }
    }

    private Notification getNotification(@NonNull final String title, final String content) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.canBypassDnd();
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            channel.setLightColor(Color.RED);
            channel.canShowBadge();
            channel.enableVibration(true);
            channel.getAudioAttributes();
            channel.getGroup();
            channel.setBypassDnd(true);
            channel.setVibrationPattern(new long[]{100, 100, 200});
            channel.shouldShowLights();

            getNotificationManager().createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id");
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(defaultSoundUri);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        return builder.build();
    }
}
