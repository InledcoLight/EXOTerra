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
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.device.DeviceActivity;
import com.inledco.exoterra.util.PrefUtil;

public class FCMMessageService extends FirebaseMessagingService {
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e("FCMMessageService", "onMessageReceived: " + remoteMessage.getFrom() + " " + remoteMessage.getTo() + " " + remoteMessage.getSentTime());
        for (String key : remoteMessage.getData().keySet()) {
            Log.e("FCMMessageService", "onMessageReceived: " + key + " : " + remoteMessage.getData().get(key));
        }
        sendNotification(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        PrefUtil.put(getApplicationContext(), AppConstants.KEY_FCM_TOKEN, s);
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
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.canBypassDnd();
//            channel.enableLights(true);
//            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
//            channel.setLightColor(Color.RED);
//            channel.canShowBadge();
//            channel.enableVibration(true);
//            channel.getAudioAttributes();
//            channel.getGroup();
//            channel.setBypassDnd(true);
//            channel.setVibrationPattern(new long[]{100, 100, 200});
//            channel.shouldShowLights();
//
//            getNotificationManager().createNotificationChannel(channel);
//        }
        String title = message.getNotification().getTitle();
        String content = message.getNotification().getBody();
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
