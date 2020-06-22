package com.inledco.exoterra.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inledco.exoterra.R;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.web.WebActivity;

public class FCMMessageService extends FirebaseMessagingService {
    private static final String TAG = "FCMMessageService";

    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e("FCMMessageService", "onMessageReceived: " + JSON.toJSONString(remoteMessage));
        for (String key : remoteMessage.getData().keySet()) {
            Log.e("FCMMessageService", "onMessageReceived: " + key + " : " + remoteMessage.getData().get(key));
        }
        sendNotification(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.e(TAG, "onNewToken: " + s);
    }

    public static void syncToken(@NonNull final FCMTokenListener listener) {
        FirebaseInstanceId.getInstance()
                          .getInstanceId()
                          .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                              @Override
                              public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                  String token = null;
                                  if (task.isSuccessful()) {
                                      token = task.getResult().getToken();
                                  }
                                  listener.onTokenResult(token);
                              }
                          });
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private void sendNotification(RemoteMessage message) {
        if (getNotificationManager() != null) {
            getNotificationManager().notify(0, getNotification(message));
        }
    }

    private Notification getNotification(RemoteMessage message) {
        PendingIntent pendingIntent = null;
        String title = message.getNotification().getTitle();
        String content = message.getNotification().getBody();
        String channelid = message.getNotification().getChannelId();
        if (TextUtils.equals(channelid, "new_product")) {
            String url = message.getData().get("url");
            Log.e(TAG, "getNotification: " + RegexUtil.isURL(url));
            if (RegexUtil.isURL(url)) {
                Intent intent = new Intent(this, WebActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.putExtra("url", url);
                intent.putExtra("allow_open_in_browser", true);
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            }
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_alarm));
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(defaultSoundUri);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        return builder.build();
    }
}
