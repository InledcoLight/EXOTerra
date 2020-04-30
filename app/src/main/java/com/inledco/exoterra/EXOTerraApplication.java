package com.inledco.exoterra;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.inledco.exoterra.base.BaseActivity;

import java.lang.ref.WeakReference;

import androidx.multidex.MultiDexApplication;

public class EXOTerraApplication extends MultiDexApplication {
    private static final String TAG = "EXOTerraApplication";

    private WeakReference<BaseActivity> mCurrentActivity;

    private NotificationManager mNotificationManager;

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

    @RequiresApi (api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

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
