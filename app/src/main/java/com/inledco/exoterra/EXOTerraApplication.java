package com.inledco.exoterra;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import com.inledco.exoterra.base.BaseActivity;
import com.inledco.exoterra.receiver.HomeInviteBroadcastReceiver;
import com.inledco.exoterra.splash.SplashActivity;

import java.lang.ref.WeakReference;

import androidx.multidex.MultiDexApplication;

public class EXOTerraApplication extends MultiDexApplication {
    private static final String TAG = "EXOTerraApplication";

    private WeakReference<BaseActivity> mCurrentActivity;

    private NotificationManager mNotificationManager;

    private BroadcastReceiver mHomeInviteReceiver = new HomeInviteBroadcastReceiver();

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

        init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(getString(R.string.notify_chnid_share), getString(R.string.notify_chn_share), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_share_receipt), getString(R.string.notify_chn_share_receipt), NotificationManager.IMPORTANCE_HIGH);
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

//    private void handleHomeShareNotify(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        Log.e(TAG, "handleHomeShareNotify: " + notify.toString());
//        if (notify.opt.equalsIgnoreCase(EventNotifyHelper.HomeMemberInvitedNotify.OPERATION_INVITED)) {
//            showReceiveHomeInviteMessage(notify);
//        }
//    }

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

//    private void showReceiveHomeInviteMessage(@NonNull final EventNotifyHelper.HomeMemberInvitedNotify notify) {
//        String from = TextUtils.isEmpty(notify.from_name) ? String.valueOf(notify.from_id) : notify.from_name;
//        String home = TextUtils.isEmpty(notify.home_name) ? notify.home_id : notify.home_name;
//        final String homeid = notify.home_id;
//        final String inviteid = notify.invite_id;
//
//        Intent acceptIntent = new Intent(this, HomeInviteBroadcastReceiver.class);
//        acceptIntent.setAction(AppConstants.HOME_INVITE);
//        acceptIntent.putExtra(AppConstants.NOTIFICATION_ID, notify.from_id);
//        acceptIntent.putExtra(AppConstants.INVITE_ID, inviteid);
//        acceptIntent.putExtra(AppConstants.HOME_ID, homeid);
//        acceptIntent.putExtra(AppConstants.ACTION, AppConstants.ACCEPT);
//        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, notify.from_id, acceptIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        Intent denyIntent = new Intent(this, HomeInviteBroadcastReceiver.class);
//        denyIntent.setAction(AppConstants.HOME_INVITE);
//        denyIntent.putExtra(AppConstants.NOTIFICATION_ID, notify.from_id);
//        denyIntent.putExtra(AppConstants.INVITE_ID, inviteid);
//        denyIntent.putExtra(AppConstants.HOME_ID, homeid);
//        denyIntent.putExtra(AppConstants.ACTION, AppConstants.DENY);
//        PendingIntent denyPendingIntent = PendingIntent.getBroadcast(this, notify.from_id^0xFFFFFFFF, denyIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notify_chnid_share_receipt));
//        Notification notification = builder.setContentTitle(getString(R.string.habitat_invite_member))
//                                           .setContentText("User " + from + " invite you join habitat " + home + ".")
//                                           .setWhen(System.currentTimeMillis())
//                                           .setSmallIcon(R.drawable.ic_device_default_black_64dp)
//                                           .setAutoCancel(true)
//                                           .setOngoing(true)
//                                           .addAction(R.drawable.ic_device_default_black_64dp, getString(R.string.accept), acceptPendingIntent)
//                                           .addAction(R.drawable.ic_device_default_black_64dp, getString(R.string.deny), denyPendingIntent)
//                                           .build();
//        getNotificationManager().notify(inviteid, notify.from_id, notification);
//    }

//    private void acceptHomeInvite(@NonNull final String homeid, @NonNull final String inviteid) {
//        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
//            return;
//        }
//        XlinkCloudManager.getInstance().acceptHomeInvite(homeid, inviteid, new XlinkRequestCallback<String>() {
//            @Override
//            public void onError(String error) {
//                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
//                     .show();
//            }
//
//            @Override
//            public void onSuccess(String s) {
//                Toast.makeText(mCurrentActivity.get(), "Join home success.", Toast.LENGTH_SHORT)
//                     .show();
//                HomeManager.getInstance().refreshHomeList(null);
//            }
//        });
//    }
//
//    private void denyHomeInvite(@NonNull final String homeid, @NonNull final String inviteid) {
//        if (mCurrentActivity == null || mCurrentActivity.get() == null) {
//            return;
//        }
//        XlinkCloudManager.getInstance().denyHomeInvite(homeid, inviteid, new XlinkRequestCallback<String>() {
//            @Override
//            public void onError(String error) {
//                Toast.makeText(mCurrentActivity.get(), error, Toast.LENGTH_SHORT)
//                     .show();
//            }
//
//            @Override
//            public void onSuccess(String s) {
//                Toast.makeText(mCurrentActivity.get(), "Deny home share success.", Toast.LENGTH_SHORT)
//                     .show();
//                HomeManager.getInstance().refreshHomeList(null);
//            }
//        });
//    }

//    private void handleHomeMessageNotify(@NonNull final EventNotifyHelper.HomeMessageNotify notify) {
//        Log.e(TAG, "handleHomeMessageNotify: " + notify.toString());
//        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMessageNotify.TYPE_DELETE)) {
//            showDeleteHomeMessage(notify);
//            HomeManager.getInstance().refreshHomeList(null);
//        }
//    }
//
//    private void handleHomeMemberChangedNotify(@NonNull final EventNotifyHelper.HomeMemberChangedNotify notify) {
//        Log.e(TAG, "handleHomeMemberChangedNotify: " + notify.toString());
//        if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_ADD)) {
//            showJoinHomeMessage(notify);
//            EventBus.getDefault().post(new HomeMemberChangedEvent(notify.home_id));
//            HomeManager.getInstance().refreshHomeList(null);
//        } else if (notify.type.equalsIgnoreCase(EventNotifyHelper.HomeMemberChangedNotify.TYPE_REMOVE)) {
//            showLeaveHomeMessage(notify);
//            EventBus.getDefault().post(new HomeMemberChangedEvent(notify.home_id));
//            HomeManager.getInstance().refreshHomeList(null);
//        }
//    }

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
//            Device device = DeviceManager.getInstance().getDeviceByDevid(notify.device_id);
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
//            Device device = DeviceManager.getInstance().getDeviceByDevid(devid);
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

    private void relogin() {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
