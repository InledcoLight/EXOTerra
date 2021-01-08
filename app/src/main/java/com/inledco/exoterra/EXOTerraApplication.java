package com.inledco.exoterra;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;

import com.inledco.exoterra.aliot.AliotClient;
import com.inledco.exoterra.event.DisconnectIotEvent;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;

import androidx.multidex.MultiDexApplication;
import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;

public class EXOTerraApplication extends MultiDexApplication {
    private static final String TAG = "EXOTerraApplication";

    private NotificationManager mNotificationManager;

    private final int PENDING_TIMEOUT = 600000;
    private int count = 0;
    private final CountDownTimer timer = new CountDownTimer(PENDING_TIMEOUT, PENDING_TIMEOUT/2) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            EventBus.getDefault().post(new DisconnectIotEvent());
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
                count++;
                timer.cancel();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (count > 0) {
                    count--;
                    if (count == 0) {
                        if (AliotClient.getInstance().isInited()) {
                            timer.start();
                        }
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (((float) dm.widthPixels)/((float) dm.heightPixels) > 9/16 ) {
            AutoSizeConfig.getInstance().setBaseOnWidth(false);
        } else {
            AutoSizeConfig.getInstance().setBaseOnWidth(true);
        }
        AutoSizeConfig.getInstance().setExcludeFontScale(true);
        AutoSize.checkAndInit(this);

        AppConfig.load(this, "AppConfig");
        GlobalSettings.init(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(getString(R.string.notify_chnid_invite), getString(R.string.notify_chn_invite), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_home_member_changed), getString(R.string.notify_chn_home_member_changed), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_delete_home), getString(R.string.notify_chn_delete_home), NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel(getString(R.string.notify_chnid_alarm), getString(R.string.notify_chn_alarm), NotificationManager.IMPORTANCE_HIGH);
        }

//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
//        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
//            @Override
//            public synchronized Map<String, String> onCrashHandleStart(int i, String s, String s1, String s2) {
//                LinkedHashMap<String, String> map = new LinkedHashMap<>();
//                return map;
//            }
//
//            @Override
//            public synchronized byte[] onCrashHandleStart2GetExtraDatas(int i, String s, String s1, String s2) {
//                return super.onCrashHandleStart2GetExtraDatas(i, s, s1, s2);
//            }
//        });
        CrashReport.setIsDevelopmentDevice(getApplicationContext(), BuildConfig.DEBUG);
        CrashReport.initCrashReport(getApplicationContext());
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
