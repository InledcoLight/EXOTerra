package com.inledco.exoterra;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.aliot.bean.InviteAction;
import com.inledco.exoterra.aliot.bean.InviteMessage;
import com.inledco.exoterra.base.BaseActivity;

import java.lang.ref.WeakReference;

import androidx.multidex.MultiDexApplication;

public class EXOTerraApplication extends MultiDexApplication {
    private static final String TAG = "EXOTerraApplication";

    private WeakReference<BaseActivity> mCurrentActivity;

    private NotificationManager mNotificationManager;

    private final BroadcastReceiver mHomeInviteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), AppConstants.HOME_INVITE)) {
                final int notification_id = intent.getIntExtra(AppConstants.NOTIFICATION_ID, 0);
                if (notification_id == 0) {
                    return;
                }
                final String message = intent.getStringExtra("InviteMessage");
                try {
                    final InviteMessage inviteMessage = JSON.parseObject(message, InviteMessage.class);
                    if (inviteMessage == null) {
                        return;
                    }
                    final InviteAction action = InviteAction.valueOf(inviteMessage.getAction());
                    if (action == null) {
                        return;
                    }
                    final String inviter = inviteMessage.getInviter();
                    final String inviteid = inviteMessage.getInvite_id();
                    final String groupid = inviteMessage.getGroupid();
                    final String groupname = inviteMessage.getGroupname();
                    switch (action) {
                        case ACCEPT:
                            AliotClient.getInstance().inviteAccept(inviter, inviteid, groupid, groupname);
                            getNotificationManager().cancel(inviteMessage.getInvite_id(), notification_id);
                            break;
                        case DENY:
                            AliotClient.getInstance().inviteDeny(inviter, inviteid, groupid, groupname);
                            getNotificationManager().cancel(inviteMessage.getInvite_id(), notification_id);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (activity instanceof BaseActivity) {
                    mCurrentActivity = new WeakReference<>((BaseActivity) activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (activity.equals(mCurrentActivity.get())) {
                    mCurrentActivity = new WeakReference<>(null);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

//        init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(getString(R.string.notify_chnid_invite), getString(R.string.notify_chn_invite), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_home_member_changed), getString(R.string.notify_chn_home_member_changed), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_delete_home), getString(R.string.notify_chn_delete_home), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_alarm), getString(R.string.notify_chn_alarm), NotificationManager.IMPORTANCE_HIGH);
        }

//        GcmRegister.register(this, AppConstants.ALIPUSH_SENDID, AppConstants.ALIPUSH_APPID);
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    private void init() {
        IntentFilter filter = new IntentFilter(AppConstants.HOME_INVITE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mHomeInviteReceiver, filter);
    }

//    private void showReceiveHomeShareDialog(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
//            return;
//        }
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        final String homeid = notify.home_id;
//        final String inviteid = notify.invite_id;
//        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity.get());
//        builder.setTitle(R.string.receive_home_share)
//               .setMessage("User " + from + " invite you join home " + home + ".")
//               .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       acceptHomeInvite(homeid, inviteid);
//                   }
//               })
//               .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
//                   @Override
//                   public void onClick(DialogInterface dialog, int which) {
//                       denyHomeInvite(homeid, inviteid);
//                   }
//               })
//               .setNeutralButton(R.string.later, null)
//               .setCancelable(false)
//               .show();
//    }

    private void showReceiveHomeInviteMessage(@NonNull final InviteMessage message) {
        final int notifyid = (int) System.currentTimeMillis();
        String from = message.getInviter();
        String groupname = message.getGroupname();
        final String groupid = message.getGroupid();
        final String inviteid = message.getInvite_id();

        Intent acceptIntent = new Intent(this, BroadcastReceiver.class);
        acceptIntent.setAction(AppConstants.HOME_INVITE);
        acceptIntent.putExtra(AppConstants.NOTIFICATION_ID, notifyid);
        message.setAction(InviteAction.ACCEPT.getAction());
        acceptIntent.putExtra("InviteMessage", JSON.toJSONString(message));
//        acceptIntent.putExtra(AppConstants.INVITE_ID, inviteid);
//        acceptIntent.putExtra(AppConstants.HOME_ID, groupid);
//        acceptIntent.putExtra(AppConstants.ACTION, InviteAction.ACCEPT.getAction());
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, notifyid, acceptIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent denyIntent = new Intent(this, BroadcastReceiver.class);
        denyIntent.setAction(AppConstants.HOME_INVITE);
        denyIntent.putExtra(AppConstants.NOTIFICATION_ID, notifyid);
        message.setAction(InviteAction.DENY.getAction());
        denyIntent.putExtra("InviteMessage", JSON.toJSONString(message));
//        denyIntent.putExtra(AppConstants.INVITE_ID, inviteid);
//        denyIntent.putExtra(AppConstants.HOME_ID, groupid);
//        denyIntent.putExtra(AppConstants.ACTION, InviteAction.DENY.getAction());
        PendingIntent denyPendingIntent = PendingIntent.getBroadcast(this, notifyid^0xFFFFFFFF, denyIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_share_receipt));
        Notification notification = builder.setContentTitle(getString(R.string.habitat_invite_member))
                                           .setContentText("User " + from + " invite you join habitat " + groupname + ".")
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_person_add_white_24dp)
                                           .setAutoCancel(true)
                                           .setOngoing(true)
                                           .addAction(R.drawable.ic_person_add_white_24dp, getString(R.string.accept), acceptPendingIntent)
                                           .addAction(R.drawable.ic_person_add_white_24dp, getString(R.string.deny), denyPendingIntent)
                                           .build();
        getNotificationManager().notify(inviteid, notifyid, notification);
    }


    @RequiresApi (api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

//    private void showHomeInviteAccpetMessage(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "invite_receipt");
//        Notification notification = builder.setContentTitle("Accpet Home2 Invite")
//                                           .setContentText("User " + from + " join home " + home + " you invite.")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .build();
//        manager.notify(3, notification);
//    }
//
//    private void showHomeInviteDenyMessage(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "invite_receipt");
//        Notification notification = builder.setContentTitle("Deny Home2 Invite")
//                                           .setContentText("User " + from + " deny home " + home + " you invite.")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .build();
//        manager.notify(4, notification);
//    }

//    private void showJoinHomeMessage(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
//        Intent intent = new Intent();
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(mCurrentActivity.get(), "home_member_changed");
//        Notification notification = builder.setContentTitle("Join Habitat")
//                                           .setContentText("User " + notify.name + " join home " + home + ".")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .setContentIntent(pendingIntent)
//                                           .build();
//        manager.notify(AppConstants.JOIN_HOME + "_" + notify.home_id, notify.user_id, notification);
//    }
//
//    private void showLeaveHomeMessage(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
//        Intent intent = new Intent();
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "home_member_changed");
//        Notification notification = builder.setContentTitle("Leave Habitat")
//                                           .setContentText("User " + notify.name + " leave home " + home + ".")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .setContentIntent(pendingIntent)
//                                           .build();
//        manager.notify(AppConstants.LEAVE_HOME + "_" + notify.home_id, notify.user_id, notification);
//    }

//    private void showDeleteHomeMessage(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
//        if (getNotificationManager() != null) {
//            Intent intent = new Intent();
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_delete_home));
//            Notification notification = builder.setContentTitle("Delete Habitat")
//                                               .setContentText("Habitat " + home + " was deleted.")
//                                               .setWhen(System.currentTimeMillis())
//                                               .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                               .setAutoCancel(true)
//                                               .setContentIntent(pendingIntent)
//                                               .build();
//            getNotificationManager().notify(AppConstants.DELETE_HOME + "_" + notify.home_id, notify.from_id, notification);
//        }
//    }
//
//    private void showOnlineStateAlarmNotification(EventNotifyHelper.OnlineStateAlertNotify notify) {
//        if (getNotificationManager() != null) {
//            XDevice device = DeviceManager.getInstance().getDeviceByDevid(notify.device_id);
//            int icon = R.drawable.ic_device_default_white_64dp;
//            if (device != null) {
//                icon = DeviceUtil.getProductIcon(device.getXDevice().getProductId());
//            }
//            Intent intent = new Intent();
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_alarm));
//            Notification notification = builder.setContentTitle(getString(R.string.notify_chn_alarm))
//                                               .setContentText(notify.msg)
//                                               .setStyle(new NotificationCompat.BigTextStyle().bigText(notify.msg))
//                                               .setWhen(System.currentTimeMillis())
//                                               .setSmallIcon(icon)
//                                               .setAutoCancel(true)
//                                               .setContentIntent(pendingIntent)
//                                               .build();
//            getNotificationManager().notify(AppConstants.DEVICE_ONLINE_STATE_ALARM, notify.device_id, notification);
//        }
//    }

//    private void showAlarmNotification(final int devid, XlinkDataPointAlertNotify notify) {
//        if (getNotificationManager() != null) {
//            XDevice device = DeviceManager.getInstance().getDeviceByDevid(devid);
//            int icon = R.drawable.ic_device_default_white_64dp;
//            if (device != null) {
//                icon = DeviceUtil.getProductIcon(device.getXDevice().getProductId());
//            }
//            Intent intent = new Intent(this, DeviceActivity.class);
//            intent.putExtra(AppConstants.DEVICE_TAG, device.getDeviceTag());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, notify.getIndex(), intent, PendingIntent.FLAG_ONE_SHOT);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_alarm));
//            Notification notification = builder.setContentTitle(getString(R.string.notify_chn_alarm))
//                                               .setContentText(notify.getMsg())
//                                               .setStyle(new NotificationCompat.BigTextStyle().bigText(notify.getMsg()))
//                                               .setWhen(System.currentTimeMillis())
//                                               .setSmallIcon(icon)
//                                               .setAutoCancel(true)
//                                               .setContentIntent(pendingIntent)
//                                               .build();
//            getNotificationManager().notify(AppConstants.DATAPOINT_ALARM + "_" + devid, notify.getIndex(), notification);
//        }
//    }
}
